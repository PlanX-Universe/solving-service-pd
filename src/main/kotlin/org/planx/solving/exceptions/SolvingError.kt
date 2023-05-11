package org.planx.solving.exceptions

import org.planx.common.models.BaseException

class SolvingError(
    override val requestId: String = "",
    override var internalErrors: String? = ""
) : BaseException()
