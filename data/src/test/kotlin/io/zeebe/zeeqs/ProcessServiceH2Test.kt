package io.zeebe.zeeqs

import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.ProcessService
import org.springframework.beans.factory.annotation.Autowired

class ProcessServiceH2Test(@Autowired override val processService: ProcessService,
                           @Autowired override val processRepository: ProcessRepository
): ProcessServiceTest(processService, processRepository)
