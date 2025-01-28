import module.Epic;
import module.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.InMemoryTaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static module.TaskStatus.*;

public class EpicTest {

    InMemoryTaskManager manager;

    @BeforeEach
    void generateData() {
        manager = new InMemoryTaskManager();
        LocalDateTime start = LocalDateTime.of(2025, 1, 28, 10, 0);
        Duration duration = Duration.ofMinutes(30);
        Epic epic1 = manager.createEpic(new Epic("EPIC1", "SOMEOFEPIC1"));
        manager.createSubtask(new Subtask("SUBTASK1", "SOMEOFSUBTASK1",
                epic1.getId(), start.minusMinutes(180), duration));
        manager.createSubtask(new Subtask("SUBTASK2", "SOMEOFSUBTASK2",
                epic1.getId(), start.minusMinutes(120), duration));
        manager.createSubtask(new Subtask("SUBTASK3", "SOMEOFSUBTASK3",
                epic1.getId(), start.minusMinutes(60), duration));
    }

    @Test
    void assertEpicHasStatusNew() {
        assertEquals(NEW, manager.getEpicById(1).getStatus(), "Неверный статус эпика");
    }

    @Test
    void assertEpicHasStatusDone() {
        manager.updateSubtask(new Subtask(2, "SUBTASK1", "SOMEOFSUBTASK1", DONE,
                1));
        manager.updateSubtask(new Subtask(3, "SUBTASK2", "SOMEOFSUBTASK2", DONE,
                1));
        manager.updateSubtask(new Subtask(4, "SUBTASK3", "SOMEOFSUBTASK3", DONE,
                1));
        assertEquals(DONE, manager.getEpicById(1).getStatus(), "Неверный статус эпика");
    }

    @Test
    void assertEpicHasStatusInProgress() {
        manager.updateSubtask(new Subtask(2, "SUBTASK1", "SOMEOFSUBTASK1", NEW,
                1));
        manager.updateSubtask(new Subtask(3, "SUBTASK2", "SOMEOFSUBTASK2", DONE,
                1));
        manager.updateSubtask(new Subtask(4, "SUBTASK3", "SOMEOFSUBTASK3", DONE,
                1));
        assertEquals(IN_PROGRESS, manager.getEpicById(1).getStatus(), "Неверный статус эпика");

        manager.updateSubtask(new Subtask(2, "SUBTASK1", "SOMEOFSUBTASK1", IN_PROGRESS,
                1));
        manager.updateSubtask(new Subtask(3, "SUBTASK2", "SOMEOFSUBTASK2", IN_PROGRESS,
                1));
        manager.updateSubtask(new Subtask(4, "SUBTASK3", "SOMEOFSUBTASK3", IN_PROGRESS,
                1));
        assertEquals(IN_PROGRESS, manager.getEpicById(1).getStatus(), "Неверный статус эпика");

        manager.updateSubtask(new Subtask(2, "SUBTASK1", "SOMEOFSUBTASK1", IN_PROGRESS,
                1));
        manager.updateSubtask(new Subtask(3, "SUBTASK2", "SOMEOFSUBTASK2", DONE,
                1));
        manager.updateSubtask(new Subtask(4, "SUBTASK3", "SOMEOFSUBTASK3", NEW,
                1));
        assertEquals(IN_PROGRESS, manager.getEpicById(1).getStatus(), "Неверный статус эпика");
    }
}
