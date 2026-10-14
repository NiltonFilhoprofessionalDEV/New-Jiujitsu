package br.com.academia.api.portal;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import br.com.academia.api.billing.InvoiceDtos.InvoiceRow;
import br.com.academia.api.billing.InvoiceDtos.Situation;
import br.com.academia.api.graduation.Graduation.Kind;
import br.com.academia.api.graduation.GraduationDtos.GraduationView;
import br.com.academia.api.graduation.GraduationDtos.Progress;
import br.com.academia.api.student.Student;
import br.com.academia.api.student.StudentEnums;
import br.com.academia.api.student.StudentEnums.Status;
import br.com.academia.api.training.ScheduleDtos.AttendanceItem;
import br.com.academia.api.training.ScheduleDtos.ScheduledClass;

/** Visões da área do aluno: só o que interessa ao aluno, sem observações internas da equipe. */
public final class PortalDtos {

	private PortalDtos() {
	}

	public record Profile(
			UUID id,
			String name,
			String nickname,
			StudentEnums.Kind kind,
			String belt,
			int degrees,
			Status status,
			boolean self) {

		static Profile of(Student s, boolean self) {
			return new Profile(s.getId(), s.getName(), s.getNickname(), s.getKind(),
					s.getBelt() == null ? null : s.getBelt().slug(), s.getDegrees(), s.getStatus(), self);
		}
	}

	public record GraduationItem(Kind kind, String belt, int degrees, LocalDate graduatedOn) {

		static GraduationItem of(GraduationView g) {
			return new GraduationItem(g.kind(), g.belt(), g.degrees(), g.graduatedOn());
		}
	}

	public record BeltProgress(LocalDate beltSince, long monthsAtBelt, long trainingsSinceBelt) {

		static BeltProgress of(Progress p) {
			return new BeltProgress(p.beltSince(), p.monthsAtBelt(), p.trainingsSinceBelt());
		}
	}

	/** {@code progress} e as listas de treino ficam vazios para responsável que não treina. */
	public record Overview(
			Profile student,
			List<ScheduledClass> classes,
			long attendanceTotal,
			List<AttendanceItem> recentAttendance,
			List<GraduationItem> graduations,
			BeltProgress progress) {
	}

	public record InvoiceItem(
			UUID id,
			String description,
			int amountCents,
			LocalDate dueDate,
			Situation situation,
			LocalDate paidOn) {

		static InvoiceItem of(InvoiceRow r) {
			return new InvoiceItem(r.id(), r.description(), r.amountCents(), r.dueDate(), r.situation(), r.paidOn());
		}
	}
}
