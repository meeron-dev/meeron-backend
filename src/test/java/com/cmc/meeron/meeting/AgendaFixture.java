package com.cmc.meeron.meeting;

import com.cmc.meeron.meeting.domain.Agenda;

public class AgendaFixture {

    public static final Agenda AGENDA = Agenda.builder()
            .id(3L)
            .agendaOrder(1)
            .build();
}
