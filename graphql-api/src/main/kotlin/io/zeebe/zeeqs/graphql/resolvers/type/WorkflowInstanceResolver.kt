package io.zeebe.zeeqs.data.resolvers

import com.coxautodev.graphql.tools.GraphQLResolver
import io.zeebe.zeeqs.data.entity.Job
import io.zeebe.zeeqs.data.entity.Variable
import io.zeebe.zeeqs.data.entity.Workflow
import io.zeebe.zeeqs.data.entity.WorkflowInstance
import io.zeebe.zeeqs.data.repository.*
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class WorkflowInstanceResolver(
        val workflowInstanceRepository: WorkflowInstanceRepository,
        val variableRepository: VariableRepository,
        val variableUpdateRepository: VariableUpdateRepository,
        val workflowRepository: WorkflowRepository,
        val jobRepository: JobRepository
) : GraphQLResolver<WorkflowInstance> {

    fun variables(workflowInstance: WorkflowInstance): List<Variable> {
        return variableRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun jobs(workflowInstance: WorkflowInstance): List<Job> {
        return jobRepository.findByWorkflowInstanceKey(workflowInstance.key)
    }

    fun workflow(workflowInstance: WorkflowInstance): Workflow? {
        return workflowRepository.findByIdOrNull(workflowInstance.workflowKey)
    }

}