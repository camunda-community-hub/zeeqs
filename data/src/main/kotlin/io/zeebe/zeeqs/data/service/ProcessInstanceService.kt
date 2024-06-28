package io.zeebe.zeeqs.data.service

import io.zeebe.zeeqs.data.entity.*
import io.zeebe.zeeqs.data.repository.ProcessInstanceKeyOnly
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ProcessInstanceService(
        private val processInstancesRepository: ProcessInstanceRepository,
        private val variableRepository: VariableRepository) {

    private fun getVariables(stateIn: List<ProcessInstanceState>, variableFilterGroup: VariableFilterGroup): List<Variable> {
        val processInstances = processInstancesRepository.findByStateIn(stateIn).toList();
        return getVariablesByProcessInstanceKeys(processInstances, variableFilterGroup);
    }

    private fun getVariables(stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variableFilterGroup: VariableFilterGroup): List<Variable> {
        val processInstances = processInstancesRepository.findByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn).toList();
        return getVariablesByProcessInstanceKeys(processInstances, variableFilterGroup);

    }

    private fun matchesFilter(variable: Variable, filter: VariableFilter): Boolean {
        return when (filter.comparisonOperation) {
            ComparisonOperation.EQUALS -> variable.name == filter.name && variable.value == filter.value
            ComparisonOperation.CONTAINS -> variable.name == filter.name && variable.value.contains(filter.value)
        }
    }

    private fun getVariablesByProcessInstanceKeys(processInstances: List<ProcessInstanceKeyOnly>, variableFilterGroup: VariableFilterGroup): List<Variable> {
        val processInstancesKeys = processInstances.map { it.getKey() }
        val variableNames = variableFilterGroup.variables.map { it.name }
        val variablesList = variableRepository.findByProcessInstanceKeyInAndNameIn(processInstancesKeys, variableNames)

        return variablesList.filter { variable ->
            if (variableFilterGroup.filterOperation == FilterOperation.AND) {
                variableFilterGroup.variables.all { matchesFilter(variable, it) }
            } else {
                variableFilterGroup.variables.any { matchesFilter(variable, it) }
            }
        }
    }


    fun getProcessInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>, variableFilterGroup: VariableFilterGroup?): List<ProcessInstance> {
        if (variableFilterGroup?.variables?.isNotEmpty() == true) {
            val filteredVariables = getVariables(stateIn, variableFilterGroup);
            val filteredProcessInstances = processInstancesRepository.findByStateInAndKeyIn(stateIn, filteredVariables.map { it.processInstanceKey }, PageRequest.of(page, perPage)).toList();
            return filteredProcessInstances;
        }
        else {
            return processInstancesRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList();
        }
    }

    fun countProcessInstances(stateIn: List<ProcessInstanceState>, variableFilterGroup: VariableFilterGroup?): Long {
        if (variableFilterGroup?.variables?.isNotEmpty() == true) {
            val filteredVariables = getVariables(stateIn, variableFilterGroup);
            return filteredVariables.count().toLong();
        }

        else {
            return processInstancesRepository.countByStateIn(stateIn);
        }
    }


    fun getProcessInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variableFilterGroup: VariableFilterGroup?): List<ProcessInstance> {
        if (variableFilterGroup?.variables?.isNotEmpty() == true) {
            val filteredVariables = getVariables(stateIn, processDefinitionKey, variableFilterGroup);
            val filteredProcessInstances = processInstancesRepository.findByStateInAndKeyIn(stateIn, filteredVariables.map { it.processInstanceKey }, PageRequest.of(page, perPage)).toList();
            return filteredProcessInstances;
        }
        else {
            return processInstancesRepository.findByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn, PageRequest.of(page, perPage)).toList();
        }
    }

    fun countProcessInstances(stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variableFilterGroup: VariableFilterGroup?): Long {
        if (variableFilterGroup?.variables?.isNotEmpty() == true) {
            val filteredVariables = getVariables(stateIn, processDefinitionKey, variableFilterGroup);
            return filteredVariables.count().toLong();
        }

        else {
            return processInstancesRepository.countByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn);
        }
    }

}