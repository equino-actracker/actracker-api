package ovh.equino.actracker.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.user.User;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class EntityEditOperationTest {

    private static final User CREATOR = new User(randomUUID());

    @Mock
    private OperationsLog operations;

    @Test
    void shouldExecuteAllStepsInSequence() {
        // given
        TestEntity testEntity = new TestEntity(CREATOR, operations);

        // when
        testEntity.edit(CREATOR);

        // then
        InOrder executions = inOrder(operations);
        executions.verify(operations).editPermissionChecked();
        executions.verify(operations).beforeEditExecuted();
        executions.verify(operations).editExecuted();
        executions.verify(operations).validationExecuted();
        executions.verify(operations).afterEditExecuted();
        executions.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopExecutionWhenEditPermissionCheckFails() {
        // given
        TestEntity testEntity = new TestEntity(CREATOR, operations);
        User unprivilegedUser = new User(randomUUID());

        // when
        assertThatThrownBy(() ->
                testEntity.edit(unprivilegedUser)
        ).isInstanceOf(RuntimeException.class);

        // then
        InOrder executions = inOrder(operations);
        executions.verify(operations).editPermissionChecked();
        executions.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopExecutionWhenBeforeEditFails() {
        // given
        TestEntity testEntity = new TestEntity(CREATOR, operations);
        doThrow(new RuntimeException()).when(operations).beforeEditExecuted();

        // when
        assertThatThrownBy(() ->
                testEntity.edit(CREATOR)
        ).isInstanceOf(RuntimeException.class);

        // then
        InOrder executions = inOrder(operations);
        executions.verify(operations).editPermissionChecked();
        executions.verify(operations).beforeEditExecuted();
        executions.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopExecutionWhenEditFails() {
        // given
        TestEntity testEntity = new TestEntity(CREATOR, operations);
        doThrow(new RuntimeException()).when(operations).editExecuted();

        // when
        assertThatThrownBy(() ->
                testEntity.edit(CREATOR)
        ).isInstanceOf(RuntimeException.class);

        // then
        InOrder executions = inOrder(operations);
        executions.verify(operations).editPermissionChecked();
        executions.verify(operations).beforeEditExecuted();
        executions.verify(operations).editExecuted();
        executions.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopExecutionWhenValidationFails() {
        // given
        TestEntity testEntity = new TestEntity(CREATOR, operations);
        doThrow(new RuntimeException()).when(operations).validationExecuted();

        // when
        assertThatThrownBy(() ->
                testEntity.edit(CREATOR)
        ).isInstanceOf(RuntimeException.class);

        // then
        InOrder executions = inOrder(operations);
        executions.verify(operations).editPermissionChecked();
        executions.verify(operations).beforeEditExecuted();
        executions.verify(operations).editExecuted();
        executions.verify(operations).validationExecuted();
        executions.verifyNoMoreInteractions();
    }

    @Test
    void shouldStopExecutionWhenAfterEditFails() {
        // given
        TestEntity testEntity = new TestEntity(CREATOR, operations);
        doThrow(new RuntimeException()).when(operations).afterEditExecuted();

        // when
        assertThatThrownBy(() ->
                testEntity.edit(CREATOR)
        ).isInstanceOf(RuntimeException.class);

        // then
        InOrder executions = inOrder(operations);
        executions.verify(operations).editPermissionChecked();
        executions.verify(operations).beforeEditExecuted();
        executions.verify(operations).editExecuted();
        executions.verify(operations).validationExecuted();
        executions.verify(operations).afterEditExecuted();
        executions.verifyNoMoreInteractions();
    }

    private class TestEntity implements Entity {

        private final User creator;
        private final OperationsLog operations;

        private TestEntity(User creator, OperationsLog operations) {
            this.creator = creator;
            this.operations = operations;
        }

        @Override
        public User creator() {
            return creator;
        }

        @Override
        public boolean isEditableFor(User user) {
            operations.editPermissionChecked();
            return Entity.super.isEditableFor(user);
        }

        @Override
        public void validate() {
            operations.validationExecuted();
        }

        private void edit(User editor) {
            new TestEntityEditOperation(editor, this, operations::editExecuted, operations)
                    .execute();
        }
    }

    private class TestEntityEditOperation extends EntityEditOperation<TestEntity> {

        private final OperationsLog operations;

        protected TestEntityEditOperation(User editor, TestEntity entity, EntityModification editOperation, OperationsLog operations) {
            super(editor, entity, editOperation);
            this.operations = operations;
        }

        @Override
        protected void beforeEditOperation() {
            operations.beforeEditExecuted();
        }

        @Override
        protected void afterEditOperation() {
            operations.afterEditExecuted();
        }
    }

    private static class OperationsLog {
        void editPermissionChecked() {
        }

        void beforeEditExecuted() {
        }

        void editExecuted() {
        }

        void validationExecuted() {
        }

        void afterEditExecuted() {
        }
    }
}