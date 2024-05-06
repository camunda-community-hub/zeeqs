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

    private fun getVariables(stateIn: List<ProcessInstanceState>, variables: List<VariableFilter>): List<Variable> {
        val processInstances = processInstancesRepository.findByStateIn(stateIn).toList();
        return getVariablesByProcessInstanceKeys(processInstances, variables);
    }

    private fun getVariables(stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variables: List<VariableFilter>): List<Variable> {
        val processInstances = processInstancesRepository.findByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn).toList();
        return getVariablesByProcessInstanceKeys(processInstances, variables);

    }

    private fun getVariablesByProcessInstanceKeys(processInstances: List<ProcessInstanceKeyOnly>, variables: List<VariableFilter>): List<Variable> {
        val variableNames = variables.map { it.name }
        val processInstancesKeys = processInstances.map { it.getKey() }
        val variablesList = variableRepository.findByProcessInstanceKeyInAndNameIn(processInstancesKeys, variableNames);
        val filteredVariables = variablesList.filter { variable ->
            variables.any { filter ->
                when (filter.equalityOperation) {
                    EqualityOperation.EQUALS -> variable.name == filter.name && variable.value == filter.value
                    EqualityOperation.CONTAINS -> variable.name == filter.name && variable.value.contains(filter.value)
                }
            }
        }
        return filteredVariables;
    }

    fun getProcessInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>, variables: List<VariableFilter>?): List<ProcessInstance> {
        if(!variables.isNullOrEmpty()) {
            val filteredVariables = getVariables(stateIn, variables);
            val filteredProcessInstances = processInstancesRepository.findByStateInAndKeyIn(stateIn, filteredVariables.map { it.processInstanceKey }, PageRequest.of(page, perPage)).toList();
            return filteredProcessInstances;
        }
        else {
            return processInstancesRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList();
        }
    }

    fun countProcessInstances(stateIn: List<ProcessInstanceState>, variables: List<VariableFilter>?): Long {
        if(!variables.isNullOrEmpty()) {
            val filteredVariables = getVariables(stateIn, variables);
            return filteredVariables.count().toLong();
        }

        else {
            return processInstancesRepository.countByStateIn(stateIn);
        }
    }


    fun getProcessInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variables: List<VariableFilter>?): List<ProcessInstance> {
        if(!variables.isNullOrEmpty()) {
            val filteredVariables = getVariables(stateIn, processDefinitionKey, variables);
            val filteredProcessInstances = processInstancesRepository.findByStateInAndKeyIn(stateIn, filteredVariables.map { it.processInstanceKey }, PageRequest.of(page, perPage)).toList();
            return filteredProcessInstances;
        }
        else {
            return processInstancesRepository.findByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn, PageRequest.of(page, perPage)).toList();
        }
    }

    fun countProcessInstances(stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variables: List<VariableFilter>?): Long {
        if(!variables.isNullOrEmpty()) {
            val filteredVariables = getVariables(stateIn, processDefinitionKey, variables);
            return filteredVariables.count().toLong();
        }

        else {
            return processInstancesRepository.countByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn);
        }
    }

}