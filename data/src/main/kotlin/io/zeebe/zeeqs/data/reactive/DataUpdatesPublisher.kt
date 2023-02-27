package io.zeebe.zeeqs.data.reactive

import io.zeebe.zeeqs.data.entity.*
import org.springframework.stereotype.Component
import java.util.function.Consumer

@Component
class DataUpdatesPublisher {

    private val processListeners = mutableListOf<Consumer<Process>>()
    private val decisionListeners = mutableListOf<Consumer<Decision>>()
    private val processInstanceListeners = mutableListOf<Consumer<ProcessInstance>>()
    private val elementInstanceListeners = mutableListOf<Consumer<ElementInstance>>()
    private val variableListeners = mutableListOf<Consumer<Variable>>()
    private val incidentListeners = mutableListOf<Consumer<Incident>>()
    private val jobListeners = mutableListOf<Consumer<Job>>()

    fun onProcessUpdated(process: Process) {
        processListeners.forEach { it.accept(process) }
    }

    fun onDecisionUpdated(decision: Decision) {
        decisionListeners.forEach { it.accept(decision) }
    }

    fun onProcessInstanceUpdated(processInstance: ProcessInstance) {
        processInstanceListeners.forEach { it.accept(processInstance) }
    }

    fun onElementInstanceUpdated(elementInstance: ElementInstance) {
        elementInstanceListeners.forEach { it.accept(elementInstance) }
    }

    fun onVariableUpdated(variable: Variable) {
        variableListeners.forEach { it.accept(variable) }
    }

    fun onIncidentUpdated(incident: Incident) {
        incidentListeners.forEach { it.accept(incident) }
    }

    fun onJobUpdated(job: Job) {
        jobListeners.forEach { it.accept(job) }
    }

    fun registerProcessListener(listener: Consumer<Process>) {
        processListeners.add(listener)
    }

    fun registerDecisionListener(listener: Consumer<Decision>) {
        decisionListeners.add(listener)
    }

    fun registerProcessInstanceListener(listener: Consumer<ProcessInstance>) {
        processInstanceListeners.add(listener)
    }

    fun registerElementInstanceListener(listener: Consumer<ElementInstance>) {
        elementInstanceListeners.add(listener)
    }

    fun registerVariableListener(listener: Consumer<Variable>) {
        variableListeners.add(listener)
    }

    fun registerIncidentListener(listener: Consumer<Incident>) {
        incidentListeners.add(listener)
    }

    fun registerJobListener(listener: Consumer<Job>) {
        jobListeners.add(listener)
    }
}