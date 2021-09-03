package io.zeebe.zeeqs

import io.zeebe.zeeqs.data.repository.ProcessRepository
import io.zeebe.zeeqs.data.service.ProcessService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("postgres-docker")
class ProcessServicePostgresTest(@Autowired override val processService: ProcessService,
                                 @Autowired override val processRepository: ProcessRepository
) : ProcessServiceTest(processService, processRepository)
