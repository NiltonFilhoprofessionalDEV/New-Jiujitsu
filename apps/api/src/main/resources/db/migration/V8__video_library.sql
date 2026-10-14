-- Biblioteca de vídeos do YouTube. O público pode ser a academia toda, algumas faixas ou algumas turmas.
create table videos (
    id          uuid primary key default gen_random_uuid(),
    academy_id  uuid          not null references academies (id) on delete cascade,
    youtube_id  varchar(11)   not null check (youtube_id ~ '^[A-Za-z0-9_-]{11}$'),
    vertical    boolean       not null default false,
    title       varchar(120)  not null,
    description varchar(2000),
    audience    varchar(10)   not null check (audience in ('ALL', 'BELTS', 'CLASSES')),
    belts       text[]        not null default '{}',
    created_by  uuid references memberships (id) on delete set null,
    created_at  timestamptz   not null default now(),
    updated_at  timestamptz   not null default now(),
    unique (academy_id, youtube_id),
    check (audience <> 'BELTS' or cardinality(belts) > 0)
);
create index ix_videos_academy on videos (academy_id, created_at desc);

create table video_audience_classes (
    video_id uuid not null references videos (id) on delete cascade,
    class_id uuid not null references training_classes (id) on delete cascade,
    primary key (video_id, class_id)
);

create table playlists (
    id          uuid primary key default gen_random_uuid(),
    academy_id  uuid         not null references academies (id) on delete cascade,
    title       varchar(80)  not null,
    description varchar(500),
    created_at  timestamptz  not null default now(),
    updated_at  timestamptz  not null default now()
);
create index ix_playlists_academy on playlists (academy_id, created_at);

-- A ordem é definida pelo gestor. Reordenar troca vídeos de posição linha a linha, então a unicidade
-- do vídeo na playlist só é conferida no commit.
create table playlist_videos (
    playlist_id uuid    not null references playlists (id) on delete cascade,
    position    integer not null check (position >= 0),
    video_id    uuid    not null references videos (id) on delete cascade,
    primary key (playlist_id, position),
    unique (playlist_id, video_id) deferrable initially deferred
);
create index ix_playlist_videos_video on playlist_videos (video_id);

-- Interações são por vínculo (conta na academia): o responsável interage em nome próprio.
create table video_reactions (
    video_id      uuid        not null references videos (id) on delete cascade,
    membership_id uuid        not null references memberships (id) on delete cascade,
    reaction      varchar(10) not null check (reaction in ('LIKE', 'FIRE', 'CLAP', 'STRONG', 'LAUGH')),
    created_at    timestamptz not null default now(),
    primary key (video_id, membership_id)
);

create table video_favorites (
    video_id      uuid        not null references videos (id) on delete cascade,
    membership_id uuid        not null references memberships (id) on delete cascade,
    created_at    timestamptz not null default now(),
    primary key (video_id, membership_id)
);
create index ix_video_favorites_member on video_favorites (membership_id);

create table video_views (
    video_id      uuid        not null references videos (id) on delete cascade,
    membership_id uuid        not null references memberships (id) on delete cascade,
    watched_at    timestamptz not null default now(),
    primary key (video_id, membership_id)
);
create index ix_video_views_member on video_views (membership_id);

create table video_comments (
    id            uuid primary key default gen_random_uuid(),
    video_id      uuid          not null references videos (id) on delete cascade,
    membership_id uuid          not null references memberships (id) on delete cascade,
    body          varchar(1000) not null check (length(btrim(body)) > 0),
    created_at    timestamptz   not null default now()
);
create index ix_video_comments_video on video_comments (video_id, created_at);
