package br.com.academia.api.platform;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface AccessGrantRepository extends JpaRepository<AccessGrant, UUID> {
}
