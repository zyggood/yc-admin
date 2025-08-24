package com.yc.admin.system.notice.service;

import com.yc.admin.common.exception.BusinessException;
import com.yc.admin.system.notice.dto.NoticeDto;
import com.yc.admin.system.notice.entity.Notice;
import com.yc.admin.system.notice.repository.NoticeRepository;
import com.yc.admin.system.notice.dto.NoticeDtoConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NoticeService单元测试
 *
 * @author yc
 */
@SpringBootTest
@ContextConfiguration(classes = com.yc.admin.AdminApplication.class)
@Transactional
@DisplayName("NoticeService单元测试")
class NoticeServiceTest {

    @MockBean
    private NoticeRepository noticeRepository;

    @MockBean
    private NoticeDtoConverter noticeDtoConverter;

    private NoticeService noticeService;

    private Notice testNotice;
    private NoticeDto testNoticeDto;
    private NoticeDto.Query testQueryDto;
    private NoticeDto.Create testCreateDto;
    private NoticeDto.Update testUpdateDto;
    private NoticeDto.StatusUpdate testStatusUpdateDto;

    @BeforeEach
    void setUp() {
        noticeService = new NoticeService(noticeRepository, noticeDtoConverter);
        
        // 初始化测试数据
        testNotice = new Notice();
        testNotice.setId(1L);
        testNotice.setNoticeTitle("测试通知");
        testNotice.setNoticeType(1);
        testNotice.setNoticeContent("测试通知内容");
        testNotice.setStatus(1);
        testNotice.setRemark("测试备注");
        testNotice.setDelFlag(0);
        testNotice.setCreateTime(LocalDateTime.now());
        testNotice.setUpdateTime(LocalDateTime.now());
        testNotice.setCreateBy("admin");
        testNotice.setUpdateBy("admin");

        testNoticeDto = new NoticeDto();
        testNoticeDto.setId(1L);
        testNoticeDto.setNoticeTitle("测试通知");
        testNoticeDto.setNoticeType(1);
        testNoticeDto.setNoticeContent("测试通知内容");
        testNoticeDto.setStatus(1);
        testNoticeDto.setRemark("测试备注");
        testNoticeDto.setCreateTime(LocalDateTime.now());
        testNoticeDto.setUpdateTime(LocalDateTime.now());
        testNoticeDto.setCreateBy("admin");
        testNoticeDto.setUpdateBy("admin");

        testQueryDto = new NoticeDto.Query();
        testQueryDto.setNoticeTitle("测试");
        testQueryDto.setNoticeType(1);
        testQueryDto.setStatus(1);

        testCreateDto = new NoticeDto.Create();
        testCreateDto.setNoticeTitle("新通知");
        testCreateDto.setNoticeType(1);
        testCreateDto.setNoticeContent("新通知内容");
        testCreateDto.setStatus(0);
        testCreateDto.setRemark("新备注");

        testUpdateDto = new NoticeDto.Update();
        testUpdateDto.setId(1L);
        testUpdateDto.setNoticeTitle("更新通知");
        testUpdateDto.setNoticeType(1);
        testUpdateDto.setNoticeContent("更新通知内容");
        testUpdateDto.setStatus(1);
        testUpdateDto.setRemark("更新备注");

        testStatusUpdateDto = new NoticeDto.StatusUpdate();
        testStatusUpdateDto.setId(1L);
        testStatusUpdateDto.setStatus(2);
    }

    @Nested
    @DisplayName("分页查询测试")
    class FindPageTests {

        @Test
        @DisplayName("正常分页查询")
        void testFindPage_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(Arrays.asList(testNotice), pageable, 1);
            Page<NoticeDto> expectedPage = new PageImpl<>(Arrays.asList(testNoticeDto), pageable, 1);
            
            when(noticeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(noticePage);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            Page<NoticeDto> result = noticeService.findPage(testQueryDto, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testNoticeDto.getId(), result.getContent().get(0).getId());
            verify(noticeRepository).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("空查询条件分页查询")
        void testFindPage_EmptyQuery() {
            // Given
            NoticeDto.Query emptyQuery = new NoticeDto.Query();
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(Arrays.asList(testNotice), pageable, 1);
            
            when(noticeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(noticePage);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            Page<NoticeDto> result = noticeService.findPage(emptyQuery, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("空结果分页查询")
        void testFindPage_EmptyResult() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            
            when(noticeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            // When
            Page<NoticeDto> result = noticeService.findPage(testQueryDto, pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("已发布通知查询测试")
    class FindPublishedNoticesTests {

        @Test
        @DisplayName("正常查询已发布通知")
        void testFindPublishedNotices_Success() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> noticePage = new PageImpl<>(Arrays.asList(testNotice), pageable, 1);
            
            when(noticeRepository.findPublishedNotices(eq(1), eq(pageable))).thenReturn(noticePage);
            when(noticeDtoConverter.toSimpleDto(any(Notice.class))).thenReturn(testNoticeDto);

            // When
            Page<NoticeDto> result = noticeService.findPublishedNotices(pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertNotNull(result.getContent().get(0));
            assertEquals(testNoticeDto.getId(), result.getContent().get(0).getId());
            verify(noticeRepository).findPublishedNotices(eq(1), eq(pageable));
        }
    }

    @Nested
    @DisplayName("ID查询测试")
    class FindByIdTests {

        @Test
        @DisplayName("正常ID查询")
        void testFindById_Success() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            Optional<NoticeDto> optionalResult = noticeService.findById(1L);
            NoticeDto result = optionalResult.orElse(null);

            // Then
            assertNotNull(result);
            assertEquals(testNoticeDto.getId(), result.getId());
            assertEquals(testNoticeDto.getNoticeTitle(), result.getNoticeTitle());
            verify(noticeRepository).findByIdAndNotDeleted(1L);
        }

        @Test
        @DisplayName("ID不存在查询")
        void testFindById_NotFound() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // When
            Optional<NoticeDto> result = noticeService.findById(999L);

            // Then
            assertFalse(result.isPresent());
            verify(noticeRepository).findByIdAndNotDeleted(999L);
        }

        @Test
        @DisplayName("空ID查询")
        void testFindById_NullId() {
            // When
            Optional<NoticeDto> result = noticeService.findById(null);

            // Then
            assertFalse(result.isPresent());
        }
    }

    @Nested
    @DisplayName("创建测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建")
        void testCreate_Success() {
            // Given
            Notice newNotice = new Notice();
            newNotice.setNoticeTitle("新通知");
            newNotice.setNoticeType(1);
            newNotice.setNoticeContent("新通知内容");
            newNotice.setStatus(0);
            newNotice.setRemark("新备注");
            newNotice.setDelFlag(0);
            
            when(noticeRepository.existsByNoticeTitleAndIdNot("新通知", null)).thenReturn(false);
            when(noticeDtoConverter.toEntity(testCreateDto)).thenReturn(newNotice);
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.create(testCreateDto);

            // Then
            assertNotNull(result);
            assertEquals(testNoticeDto.getId(), result.getId());
            verify(noticeRepository).existsByNoticeTitleAndIdNot("新通知", null);
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("标题重复创建")
        void testCreate_DuplicateTitle() {
            // Given
            when(noticeRepository.existsByNoticeTitleAndIdNot("新通知", null)).thenReturn(true);

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.create(testCreateDto));
            verify(noticeRepository).existsByNoticeTitleAndIdNot("新通知", null);
            verify(noticeRepository, never()).save(any(Notice.class));
        }

        @Test
        @DisplayName("空数据创建")
        void testCreate_NullData() {
            // When & Then
            assertThrows(NullPointerException.class, () -> noticeService.create(null));
        }
    }

    @Nested
    @DisplayName("更新测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新")
        void testUpdate_Success() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeRepository.existsByNoticeTitleAndIdNot("更新通知", 1L)).thenReturn(false);
            when(noticeDtoConverter.updateEntity(any(NoticeDto.Update.class), any(Notice.class))).thenReturn(testNotice);
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(any(Notice.class))).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.update(testUpdateDto);

            // Then
            assertNotNull(result);
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository).existsByNoticeTitleAndIdNot("更新通知", 1L);
            verify(noticeDtoConverter).updateEntity(any(NoticeDto.Update.class), any(Notice.class));
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("更新不存在的记录")
        void testUpdate_NotFound() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.update(testUpdateDto));
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }

        @Test
        @DisplayName("更新标题重复")
        void testUpdate_DuplicateTitle() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeRepository.existsByNoticeTitleAndIdNot("更新通知", 1L)).thenReturn(true);

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.update(testUpdateDto));
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository).existsByNoticeTitleAndIdNot("更新通知", 1L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("状态更新测试")
    class UpdateStatusTests {

        @Test
        @DisplayName("正常状态更新")
        void testUpdateStatus_Success() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeDtoConverter.updateStatus(any(NoticeDto.StatusUpdate.class), any(Notice.class))).thenReturn(testNotice);
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(any(Notice.class))).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.updateStatus(testStatusUpdateDto);

            // Then
            assertNotNull(result);
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeDtoConverter).updateStatus(any(NoticeDto.StatusUpdate.class), any(Notice.class));
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("更新不存在记录的状态")
        void testUpdateStatus_NotFound() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.updateStatus(testStatusUpdateDto));
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("删除测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除")
        void testDelete_Success() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));

            // When
            assertDoesNotThrow(() -> noticeService.delete(1L));

            // Then
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("删除不存在的记录")
        void testDelete_NotFound() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.delete(999L));
            verify(noticeRepository).findByIdAndNotDeleted(999L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("发布测试")
    class PublishTests {

        @Test
        @DisplayName("正常发布")
        void testPublish_Success() {
            // Given
            testNotice.setStatus(0); // 草稿状态
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.publish(1L);

            // Then
            assertNotNull(result);
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("发布不存在的记录")
        void testPublish_NotFound() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.publish(999L));
            verify(noticeRepository).findByIdAndNotDeleted(999L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }

        @Test
        @DisplayName("发布已发布的记录")
        void testPublish_AlreadyPublished() {
            // Given
            testNotice.setStatus(1); // 已发布状态
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.publish(1L));
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("关闭测试")
    class CloseTests {

        @Test
        @DisplayName("正常关闭")
        void testClose_Success() {
            // Given
            testNotice.setStatus(1); // 已发布状态
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.close(1L);

            // Then
            assertNotNull(result);
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("关闭不存在的记录")
        void testClose_NotFound() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.close(999L));
            verify(noticeRepository).findByIdAndNotDeleted(999L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }

        @Test
        @DisplayName("关闭草稿状态的记录")
        void testClose_DraftStatus() {
            // Given
            testNotice.setStatus(0); // 草稿状态
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));

            // When & Then
            assertThrows(BusinessException.class, () -> noticeService.close(1L));
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeRepository, never()).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("异常处理测试")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("数据库异常处理")
        void testDatabaseException() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L))
                    .thenThrow(new RuntimeException("数据库连接异常"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                Optional<NoticeDto> result = noticeService.findById(1L);
                result.orElse(null);
            });
        }

        @Test
        @DisplayName("转换器异常处理")
        void testConverterException() {
            // Given
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeDtoConverter.toDto(testNotice))
                    .thenThrow(new RuntimeException("转换异常"));

            // When & Then
            assertThrows(RuntimeException.class, () -> {
                Optional<NoticeDto> result = noticeService.findById(1L);
                result.orElse(null);
            });
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class BoundaryConditionTests {

        @Test
        @DisplayName("最大页码查询")
        void testFindPage_MaxPageSize() {
            // Given
            Pageable pageable = PageRequest.of(Integer.MAX_VALUE, 1000);
            Page<Notice> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            
            when(noticeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

            // When
            Page<NoticeDto> result = noticeService.findPage(testQueryDto, pageable);

            // Then
            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
        }

        @Test
        @DisplayName("最长标题测试")
        void testCreate_MaxTitleLength() {
            // Given
            String maxTitle = "a".repeat(100); // 最大长度100
            testCreateDto.setNoticeTitle(maxTitle);
            
            Notice newNotice = new Notice();
            newNotice.setNoticeTitle(maxTitle);
            when(noticeRepository.existsByNoticeTitleAndIdNot(maxTitle, null)).thenReturn(false);
            when(noticeDtoConverter.toEntity(testCreateDto)).thenReturn(newNotice);
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.create(testCreateDto);

            // Then
            assertNotNull(result);
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("最长备注测试")
        void testCreate_MaxRemarkLength() {
            // Given
            String maxRemark = "a".repeat(500); // 最大长度500
            testCreateDto.setRemark(maxRemark);
            
            Notice newNotice = new Notice();
            newNotice.setRemark(maxRemark);
            when(noticeRepository.existsByNoticeTitleAndIdNot("新通知", null)).thenReturn(false);
            when(noticeDtoConverter.toEntity(testCreateDto)).thenReturn(newNotice);
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(testNotice)).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.create(testCreateDto);

            // Then
            assertNotNull(result);
            verify(noticeRepository).save(any(Notice.class));
        }

        @Test
        @DisplayName("状态边界值测试")
        void testUpdateStatus_BoundaryValues() {
            // Given
            testStatusUpdateDto.setStatus(2); // 关闭状态
            when(noticeRepository.findByIdAndNotDeleted(1L)).thenReturn(Optional.of(testNotice));
            when(noticeDtoConverter.updateStatus(any(NoticeDto.StatusUpdate.class), any(Notice.class))).thenReturn(testNotice);
            when(noticeRepository.save(any(Notice.class))).thenReturn(testNotice);
            when(noticeDtoConverter.toDto(any(Notice.class))).thenReturn(testNoticeDto);

            // When
            NoticeDto result = noticeService.updateStatus(testStatusUpdateDto);

            // Then
            assertNotNull(result);
            verify(noticeRepository).findByIdAndNotDeleted(1L);
            verify(noticeDtoConverter).updateStatus(any(NoticeDto.StatusUpdate.class), any(Notice.class));
            verify(noticeRepository).save(any(Notice.class));
        }
    }
}