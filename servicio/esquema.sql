-- Alarma2: tablas y reglas del servicio (Supabase). Se pega en el editor SQL del proyecto.
-- Reglas: cada uno cambia solo lo suyo; las alarmas las ven solo los amigos;
-- al borrar la amistad se corta el acceso en el acto (la regla se evalúa en cada lectura).

-- Perfiles: un @ID único y un nombre visible por cuenta.
create table if not exists public.perfiles (
  id uuid primary key references auth.users (id) on delete cascade,
  usuario text not null unique check (usuario ~ '^[a-z0-9_.]{3,20}$'),
  nombre text not null check (char_length(nombre) between 1 and 40),
  creado timestamptz not null default now()
);

-- Amistades: una fila por par. Estado 'pendiente' (solicitud de -> para) o 'aceptada'.
create table if not exists public.amistades (
  de uuid not null references public.perfiles (id) on delete cascade,
  para uuid not null references public.perfiles (id) on delete cascade,
  estado text not null default 'pendiente' check (estado in ('pendiente', 'aceptada')),
  creado timestamptz not null default now(),
  primary key (de, para),
  check (de <> para)
);
-- Una sola fila por par, sin importar quién pidió.
create unique index if not exists amistades_par on public.amistades (least(de, para), greatest(de, para));

-- Alarmas subidas por su dueño (copia; la que suena es la del celular).
create table if not exists public.alarmas (
  dueno uuid not null references public.perfiles (id) on delete cascade default auth.uid(),
  local_id int not null,
  hora int not null check (hora between 0 and 23),
  minuto int not null check (minuto between 0 and 59),
  dias int not null default 0,          -- bits: lunes=1 ... domingo=64; 0 = una sola vez
  activa boolean not null default true,
  proximo timestamptz,                  -- próximo disparo según el celular del dueño
  actualizado timestamptz not null default now(),
  primary key (dueno, local_id)
);

-- ¿a y b son amigos (aceptada)?
create or replace function public.son_amigos(a uuid, b uuid) returns boolean
language sql stable security definer set search_path = public as $$
  select exists (
    select 1 from public.amistades
    where estado = 'aceptada' and ((de = a and para = b) or (de = b and para = a))
  );
$$;
revoke all on function public.son_amigos(uuid, uuid) from public, anon;
grant execute on function public.son_amigos(uuid, uuid) to authenticated;

alter table public.perfiles enable row level security;
alter table public.amistades enable row level security;
alter table public.alarmas enable row level security;

-- Nadie sin cuenta toca nada.
revoke all on public.perfiles, public.amistades, public.alarmas from anon;
grant select, insert, update, delete on public.perfiles, public.amistades, public.alarmas to authenticated;

-- Perfiles: con cuenta se puede buscar por @ID (solo @ID y nombre); cada uno crea y cambia el suyo.
drop policy if exists perfiles_ver on public.perfiles;
create policy perfiles_ver on public.perfiles for select to authenticated using (true);
drop policy if exists perfiles_crear on public.perfiles;
create policy perfiles_crear on public.perfiles for insert to authenticated with check (id = auth.uid());
drop policy if exists perfiles_cambiar on public.perfiles;
create policy perfiles_cambiar on public.perfiles for update to authenticated using (id = auth.uid()) with check (id = auth.uid());

-- Amistades: cada uno ve las suyas; solo pide a nombre propio; solo el que recibe acepta;
-- cualquiera de los dos borra (rechazar, cancelar o sacar amigo).
drop policy if exists amistades_ver on public.amistades;
create policy amistades_ver on public.amistades for select to authenticated using (auth.uid() in (de, para));
drop policy if exists amistades_pedir on public.amistades;
create policy amistades_pedir on public.amistades for insert to authenticated with check (de = auth.uid() and estado = 'pendiente');
drop policy if exists amistades_aceptar on public.amistades;
create policy amistades_aceptar on public.amistades for update to authenticated using (para = auth.uid()) with check (para = auth.uid() and estado = 'aceptada');
drop policy if exists amistades_borrar on public.amistades;
create policy amistades_borrar on public.amistades for delete to authenticated using (auth.uid() in (de, para));

-- Que al aceptar no se pueda cambiar quién es quién.
create or replace function public.amistad_fija() returns trigger language plpgsql as $$
begin
  if new.de <> old.de or new.para <> old.para then
    raise exception 'no se puede cambiar la amistad';
  end if;
  return new;
end $$;
drop trigger if exists amistad_fija on public.amistades;
create trigger amistad_fija before update on public.amistades for each row execute function public.amistad_fija();

-- Alarmas: el dueño hace todo con las suyas; los amigos solo las ven.
drop policy if exists alarmas_dueno on public.alarmas;
create policy alarmas_dueno on public.alarmas for all to authenticated using (dueno = auth.uid()) with check (dueno = auth.uid());
drop policy if exists alarmas_amigos on public.alarmas;
create policy alarmas_amigos on public.alarmas for select to authenticated using (public.son_amigos(auth.uid(), dueno));

-- Aceptar una solicitud recibida (corre con los permisos de quien llama: las reglas de arriba siguen valiendo).
create or replace function public.aceptar_amistad(otro uuid) returns void
language sql security invoker set search_path = public as $$
  update public.amistades set estado = 'aceptada' where de = otro and para = auth.uid() and estado = 'pendiente';
$$;
revoke all on function public.aceptar_amistad(uuid) from public, anon;
grant execute on function public.aceptar_amistad(uuid) to authenticated;
