package com.aria.service;

import com.aria.dao.FgmDataRepository;
import com.aria.model.FgmData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FgmDataService {

    @Autowired
    private FgmDataRepository fgmDataRepository;

    /**
     * Get all FgmData records
     *
     * @return List of FgmData
     */
    public List<FgmData> getAllFgmData() {
        return fgmDataRepository.findAll();
    }

    /**
     * Get FgmData by ID
     *
     * @param id the FgmData ID
     * @return Optional of FgmData
     */
    public Optional<FgmData> getFgmDataById(Long id) {
        return fgmDataRepository.findById(id);
    }

    /**
     * Create a new FgmData record
     *
     * @param fgmData the FgmData object to save
     * @return the saved FgmData object
     */
    public FgmData createFgmData(FgmData fgmData) {
        return fgmDataRepository.save(fgmData);
    }

    /**
     * Update an existing FgmData record
     *
     * @param id      the FgmData ID
     * @param fgmData the FgmData object with updated values
     * @return the updated FgmData object or null if not found
     */
    public FgmData updateFgmData(Long id, FgmData fgmData) {
        if (fgmDataRepository.existsById(id)) {
            fgmData.setId(id);
            return fgmDataRepository.save(fgmData);
        }
        return null;
    }

    /**
     * Delete a FgmData record
     *
     * @param id the FgmData ID
     */
    public void deleteFgmData(Long id) {
        fgmDataRepository.deleteById(id);
    }
}
