package org.white_powerbank.models

enum class PartnerSearchStatus(var statusId: Int) {
    ACTIVE(1),
    INACTIVE(2),
    FOUND(3);

    companion object {
        fun fromId(id: Int): PartnerSearchStatus {
            return entries.firstOrNull { it.statusId == id } ?: PartnerSearchStatus.INACTIVE
        }
    }
}