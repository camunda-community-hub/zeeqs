package io.zeebe.zeeqs.data.service

import io.zeebe.zeeqs.data.entity.ProcessInstance
import io.zeebe.zeeqs.data.entity.ProcessInstanceState
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.repository.ProcessInstanceKeyOnly
import io.zeebe.zeeqs.data.repository.ProcessInstanceRepository
import io.zeebe.zeeqs.data.repository.VariableRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class ProcessInstanceService(
        private val processInstancesRepository: ProcessInstanceRepository,
        private val variableRepository: VariableRepository) {

    private fun getVariables(stateIn: List<ProcessInstanceState>, variableName: String, variableValue: String): List<Variable> {
        val processInstances = processInstancesRepository.findByStateIn(stateIn).toList();
        return getVariablesByProcessInstanceKeys(processInstances, variableName, variableValue);
    }

    private fun getVariables(stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variableName: String, variableValue: String): List<Variable> {
        val processInstances = processInstancesRepository.findByProcessDefinitionKeyAndStateIn(processDefinitionKey, stateIn).toList();
        return getVariablesByProcessInstanceKeys(processInstances, variableName, variableValue);

    }

    private fun getVariablesByProcessInstanceKeys(processInstances: List<ProcessInstanceKeyOnly>, variableName: String, variableValue: String): List<Variable> {
        val variables = variableRepository.findByProcessInstanceKeyInAndName(processInstances.map { it.getKey() }, variableName);
        val filteredVariables = variables.filter { it.value == variableValue };
        return filteredVariables;
    }

    fun getProcessInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>, variableName: String?, variableValue: String?): List<ProcessInstance> {
        if(variableName != null && variableValue != null) {
            val filteredVariables = getVariables(stateIn, variableName, variableValue);
            val filteredProcessInstances = processInstancesRepository.findByStateInAndKeyIn(stateIn, filteredVariables.map { it.processInstanceKey }, PageRequest.of(page, perPage)).toList();
            return filteredProcessInstances;
        }
        else {
            return processInstancesRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList();
        }
    }

    fun countProcessInstances(stateIn: List<ProcessInstanceState>, variableName: String?, variableValue: String?): Long {
        if(variableName != null && variableValue != null) {
            val filteredVariables = getVariables(stateIn, variableName, variableValue);
            return filteredVariables.count().toLong();
        }

        else {
            return processInstancesRepository.countByStateIn(stateIn);
        }
    }


    fun getProcessInstances(perPage: Int, page: Int, stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variableName: String?, variableValue: String?): List<ProcessInstance> {
        if(variableName != null && variableValue != null) {
            val filteredVariables = getVariables(stateIn, processDefinitionKey, variableName, variableValue);
            val filteredProcessInstances = processInstancesRepository.findByStateInAndKeyIn(stateIn, filteredVariables.map { it.processInstanceKey }, PageRequest.of(page, perPage)).toList();
            return filteredProcessInstances;
        }
        else {
            return processInstancesRepository.findByStateIn(stateIn, PageRequest.of(page, perPage)).toList();
        }
    }

    fun countProcessInstances(stateIn: List<ProcessInstanceState>, processDefinitionKey: Long, variableName: String?, variableValue: String?): Long {
        if(variableName != null && variableValue != null) {
            val filteredVariables = getVariables(stateIn, processDefinitionKey, variableName, variableValue);
            return filteredVariables.count().toLong();
        }

        else {
            return processInstancesRepository.countByStateIn(stateIn);
        }
    }

}