package com.cmc.meeron.file.application.service;

import com.cmc.meeron.common.exception.file.FileExtensionNotFoundException;
import com.cmc.meeron.file.application.port.out.AgendaFileCommandPort;
import com.cmc.meeron.file.application.port.out.StoragePort;
import com.cmc.meeron.file.domain.AgendaFile;
import com.cmc.meeron.meeting.application.port.out.MeetingQueryPort;
import com.cmc.meeron.meeting.domain.Agenda;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static com.cmc.meeron.file.FileFixture.FILE;
import static com.cmc.meeron.meeting.AgendaFixture.AGENDA;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock StoragePort storagePort;
    @Mock AgendaFileCommandPort agendaFileCommandPort;
    @Mock MeetingQueryPort meetingQueryPort;
    @InjectMocks FileService fileService;

    private Agenda agenda;
    private List<MultipartFile> files;

    @BeforeEach
    void setUp() {
        agenda = AGENDA;
        files = List.of(FILE, FILE);
    }

    @DisplayName("아젠다 파일 생성 - 실패 / 파일의 확장자 명을 찾을 수 없을 경우")
    @Test
    void create_agenda_files_fail_not_found_file_extension() throws Exception {

        // given
        when(meetingQueryPort.findAgendaById(any()))
                .thenReturn(Optional.of(agenda));
        MockMultipartFile notExtensionFile = new MockMultipartFile("1", "1".getBytes());

        // when, then
        assertThrows(
                FileExtensionNotFoundException.class,
                () -> fileService.saveAgendaFiles(any(), List.of(notExtensionFile))
        );
    }

    @DisplayName("아젠다 파일 생성 - 실패 / 파일 업로드 중 예외가 발생했을 경우")
    @Test
    void create_agenda_files_fail_upload() throws Exception {

        // given
        when(meetingQueryPort.findAgendaById(any()))
                .thenReturn(Optional.of(agenda));
        doThrow(new FileExtensionNotFoundException())
                .when(storagePort)
                .upload(any(), any(), any());

        // when, then
        assertThrows(
                FileExtensionNotFoundException.class,
                () -> fileService.saveAgendaFiles(any(), files)
        );
    }

    @DisplayName("아젠다 파일 생성 - 성공")
    @Test
    void create_agenda_files_success() throws Exception {

        // given
        when(storagePort.getUrl(any()))
                .thenReturn("테스트파일");
        when(meetingQueryPort.findAgendaById(any()))
                .thenReturn(Optional.of(agenda));

        // when
        fileService.saveAgendaFiles(any(), files);

        // then
        assertAll(
                () -> verify(storagePort, times(2)).upload(any(), any(), any()),
                () -> verify(meetingQueryPort).findAgendaById(any()),
                () -> verify(agendaFileCommandPort, times(2)).save(any(AgendaFile.class))
        );
    }

    @DisplayName("프로필 이미지 저장 - 성공")
    @Test
    void save_profile_image_success() throws Exception {

        // given
        MultipartFile file = FILE;

        // when
        String savedImageUrl = fileService.saveProfileImage(file);

        // then
        assertAll(
                () -> verify(storagePort).upload(any(), any(), any()),
                () -> verify(storagePort).getUrl(any())
        );
    }
}
