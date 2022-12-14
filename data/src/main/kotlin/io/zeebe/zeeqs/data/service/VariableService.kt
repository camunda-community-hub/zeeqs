package io.zeebe.zeeqs.data.service

import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.ElementInstanceRepository
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class VariableService(
        private val variableRepository: VariableRepository,
        private val elementInstanceRepository: ElementInstanceRepository) {

    fun getVariables(elementInstanceKey: Long, localOnly: Boolean, shadowing: Boolean): List<Variable> {
        val localVariables = variableRepository.findByScopeKey(scopeKey = elementInstanceKey)

        if (localOnly) {
            return localVariables
        }

        val localVariableNames = localVariables.map { it.name }

        return elementInstanceRepository.findByIdOrNull(elementInstanceKey)
                ?.scopeKey
                ?.let { flowScopeKey ->
                    getVariables(
                            elementInstanceKey = flowScopeKey,
                            localOnly = false,
                            shadowing = shadowing)
                }
                ?.filterNot { flowScopeVariable ->
                    shadowing && localVariableNames.contains(flowScopeVariable.name)
                }
                ?.let { flowScopeVariables -> flowScopeVariables + localVariables }
                ?: localVariables
    }

}