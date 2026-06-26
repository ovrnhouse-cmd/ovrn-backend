package com.Ishwarjit.Wolf_OVRN_backend.service;

import com.Ishwarjit.Wolf_OVRN_backend.dto.StoreStatusResponse;
import com.Ishwarjit.Wolf_OVRN_backend.dto.UpdateStoreStatusRequest;
import com.Ishwarjit.Wolf_OVRN_backend.entity.StoreStatus;
import com.Ishwarjit.Wolf_OVRN_backend.repository.StoreStatusRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreStatusService {

    private final StoreStatusRepository storeStatusRepository;

    public StoreStatusService(StoreStatusRepository storeStatusRepository) {
        this.storeStatusRepository = storeStatusRepository;
    }

    @Transactional(readOnly = true)
    public StoreStatusResponse getStatus() {
        return StoreStatusResponse.from(getOrCreateStatus());
    }

    @Transactional
    public StoreStatusResponse updateStatus(UpdateStoreStatusRequest request) {
        StoreStatus status = getOrCreateStatus();

        if (request.getIsTakingOrders() != null) {
            status.setTakingOrders(request.getIsTakingOrders());
        }
        if (request.getIsMaintenanceMode() != null) {
            status.setMaintenanceMode(request.getIsMaintenanceMode());
        }
        if (request.getStatusMessage() != null) {
            // empty string means clear the message
            if (request.getStatusMessage().isBlank()) {
                status.setStatusMessage(null);
            } else {
                status.setStatusMessage(request.getStatusMessage());
            }
        }
        if (request.getFreeShippingThreshold() != null) {
            status.setFreeShippingThreshold(request.getFreeShippingThreshold());
        }
        if (request.getStandardShippingFee() != null) {
            status.setStandardShippingFee(request.getStandardShippingFee());
        }

        return StoreStatusResponse.from(storeStatusRepository.save(status));
    }

    public StoreStatus getOrCreateStatus() {
        List<StoreStatus> all = storeStatusRepository.findAll();
        if (!all.isEmpty()) {
            return all.get(0);
        }
        StoreStatus defaultStatus = new StoreStatus();
        return storeStatusRepository.save(defaultStatus);
    }
}
