package io.zeebe.zeeqs

import io.zeebe.zeeqs.data.entity.BpmnElementType
import io.zeebe.zeeqs.data.entity.ElementInstance
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.VariableRepository
import io.zeebe.zeeqs.data.service.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import java.util.stream.LongStream

import org.assertj.core.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired

@SpringBootTest
@TestConfiguration
class VariableServiceTest(
        @Autowired val variableService: VariableService,
        @Autowired val variableRepository: VariableRepository,
        @Autowired val elementInstanceRepository: ElementInstanceRepository
) {

    private val processInstanceKey = 10L
    private val scope_1_1 = 11L
    private val scope_1_2 = 12L
    private val scope_1_3 = 13L
    private val scope_2_1 = 21L

    private val nextVariableKey = LongStream.range(1, 100).iterator()

    @BeforeEach
    fun `create variables`() {
        createVariable(name = "global_1", value = "1", scopeKey = processInstanceKey)
        createVariable(name = "global_2", value = "2", scopeKey = processInstanceKey)
        createVariable(name = "var_1_1", value = "1_1", scopeKey = scope_1_1)
        createVariable(name = "var_1_2", value = "1_2", scopeKey = scope_1_2)
        createVariable(name = "var_1_1", value = "1_3", scopeKey = scope_1_3)
        createVariable(name = "var_1_2", value = "1_3", scopeKey = scope_1_3)
        createVariable(name = "var_1_3", value = "1_3", scopeKey = scope_1_3)
        createVariable(name = "var_2_1", value = "2_1", scopeKey = scope_2_1)

        createScope(key = processInstanceKey, scopeKey = null)
        createScope(key = scope_1_1, scopeKey = processInstanceKey)
        createScope(key = scope_1_2, scopeKey = scope_1_1)
        createScope(key = scope_1_3, scopeKey = scope_1_2)
        createScope(key = scope_2_1, scopeKey = processInstanceKey)
    }

    @Test
    fun `get global variables`() {
        // when/then
        assertThat(
                variableService.getVariables(
                        elementInstanceKey = processInstanceKey,
                        localOnly = true,
                        shadowing = false))
                .hasSize(2)
                .extracting(Variable::name, Variable::value)
                .contains(
                        tuple("global_1", "1"),
                        tuple("global_2", "2")
                )
    }

    @Test
    fun `get local variables`() {
        // when/then
        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_1,
                        localOnly = true,
                        shadowing = false))
                .hasSize(1)
                .extracting(Variable::name, Variable::value)
                .contains(tuple("var_1_1", "1_1"))

        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_2,
                        localOnly = true,
                        shadowing = false))
                .hasSize(1)
                .extracting(Variable::name, Variable::value)
                .contains(tuple("var_1_2", "1_2"))

        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_3,
                        localOnly = true,
                        shadowing = false))
                .hasSize(3)
                .extracting(Variable::name, Variable::value)
                .contains(
                        tuple("var_1_1", "1_3"),
                        tuple("var_1_2", "1_3"),
                        tuple("var_1_3", "1_3")
                )
    }

    @Test
    fun `get all variables`() {
        // when/then
        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_1,
                        localOnly = false,
                        shadowing = false))
                .hasSize(3)
                .extracting(Variable::name, Variable::value, Variable::scopeKey)
                .contains(
                        tuple("global_1", "1", processInstanceKey),
                        tuple("global_2", "2", processInstanceKey),
                        tuple("var_1_1", "1_1", scope_1_1)
                )

        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_2,
                        localOnly = false,
                        shadowing = false))
                .hasSize(4)
                .extracting(Variable::name, Variable::value, Variable::scopeKey)
                .contains(
                        tuple("global_1", "1", processInstanceKey),
                        tuple("global_2", "2", processInstanceKey),
                        tuple("var_1_1", "1_1", scope_1_1),
                        tuple("var_1_2", "1_2", scope_1_2)
                )

        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_3,
                        localOnly = false,
                        shadowing = false))
                .hasSize(7)
                .extracting(Variable::name, Variable::value, Variable::scopeKey)
                .contains(
                        tuple("global_1", "1", processInstanceKey),
                        tuple("global_2", "2", processInstanceKey),
                        tuple("var_1_1", "1_1", scope_1_1),
                        tuple("var_1_2", "1_2", scope_1_2),
                        tuple("var_1_1", "1_3", scope_1_3),
                        tuple("var_1_2", "1_3", scope_1_3),
                        tuple("var_1_3", "1_3", scope_1_3)
                )
    }

    @Test
    fun `get all variables with shadowing`() {
        // when/then
        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_1,
                        localOnly = false,
                        shadowing = true))
                .hasSize(3)
                .extracting(Variable::name, Variable::value, Variable::scopeKey)
                .contains(
                        tuple("global_1", "1", processInstanceKey),
                        tuple("global_2", "2", processInstanceKey),
                        tuple("var_1_1", "1_1", scope_1_1)
                )

        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_2,
                        localOnly = false,
                        shadowing = true))
                .hasSize(4)
                .extracting(Variable::name, Variable::value, Variable::scopeKey)
                .contains(
                        tuple("global_1", "1", processInstanceKey),
                        tuple("global_2", "2", processInstanceKey),
                        tuple("var_1_1", "1_1", scope_1_1),
                        tuple("var_1_2", "1_2", scope_1_2)
                )

        assertThat(
                variableService.getVariables(
                        elementInstanceKey = scope_1_3,
                        localOnly = false,
                        shadowing = true))
                .hasSize(5)
                .extracting(Variable::name, Variable::value, Variable::scopeKey)
                .contains(
                        tuple("global_1", "1", processInstanceKey),
                        tuple("global_2", "2", processInstanceKey),
                        tuple("var_1_1", "1_3", scope_1_3),
                        tuple("var_1_2", "1_3", scope_1_3),
                        tuple("var_1_3", "1_3", scope_1_3)
                )
    }

    private fun createVariable(name: String, value: String, scopeKey: Long) {
        variableRepository.save(
                Variable(
                        name = name,
                        value = value,
                        scopeKey = scopeKey,
                        processInstanceKey = processInstanceKey,
                        key = nextVariableKey.next(),
                        position = 2L,
                        timestamp = 1L
                )
        )
    }

    private fun createScope(key: Long, scopeKey: Long?) {
        elementInstanceRepository.save(ElementInstance(
                key = key,
                position = 1,
                elementId = "",
                bpmnElementType = BpmnElementType.UNSPECIFIED,
                processInstanceKey = processInstanceKey,
                processDefinitionKey = 1L,
                scopeKey = scopeKey
        ))
    }

    @SpringBootApplication
    class TestConfiguration

}
