package br.com.academia.api.billing;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

	boolean existsByStudentId(UUID studentId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select i from Invoice i where i.id = ?1 and i.academyId = ?2")
	Optional<Invoice> findForUpdate(UUID id, UUID academyId);

	/** Por aluno: a dívida vencida (desde quando e quanto) e o próximo vencimento ainda no prazo. */
	@Query("""
			select new br.com.academia.api.billing.OpenBalance(i.studentId,
			    min(case when i.dueDate < ?2 then i.dueDate end),
			    sum(case when i.dueDate < ?2 then i.amountCents else 0 end),
			    min(case when i.dueDate >= ?2 then i.dueDate end))
			from Invoice i
			where i.academyId = ?1 and i.status = br.com.academia.api.billing.Invoice.Status.OPEN
			group by i.studentId
			""")
	List<OpenBalance> openBalances(UUID academyId, LocalDate today);
}
