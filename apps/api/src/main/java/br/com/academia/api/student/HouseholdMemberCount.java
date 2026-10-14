package br.com.academia.api.student;

import java.util.UUID;

public record HouseholdMemberCount(UUID householdId, long members) {
}
