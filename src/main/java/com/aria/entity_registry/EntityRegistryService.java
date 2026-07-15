package com.aria.entity_registry;

import com.aria.entity_registry.dao.EntityFieldRegistryRepository;
import com.aria.entity_registry.dao.EntityRegistryRepository;
import com.aria.entity_registry.model.EntityFieldRegistryEntry;
import com.aria.entity_registry.model.EntityRegistryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Read API over the entity + field registries (Schema V2 §18 #3 + #4).
 *
 * <p>Used by:
 * <ul>
 *   <li>{@link com.aria.datasource.DataSourceQueryService} — resolves
 *       Vendor → m0_vendor at query-compile time.</li>
 *   <li>Builder UI (Round 1G.2+) — populates the field palette.</li>
 * </ul>
 */
@Service
public class EntityRegistryService {

    @Autowired private EntityRegistryRepository entities;
    @Autowired private EntityFieldRegistryRepository fields;

    /** Returns the entity rows along with their fields, suitable for the
     *  Builder palette. Field list nests under each entity row by code. */
    public List<Map<String, Object>> listAllWithFields() {
        Map<String, Map<String, Object>> byCode = new LinkedHashMap<>();
        for (EntityRegistryEntry e : entities.findAllByOrderByCodeAsc()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", e.getCode());
            m.put("displayName", e.getDisplayName());
            m.put("tableName", e.getTableName());
            m.put("category", e.getCategory());
            m.put("description", e.getDescription());
            m.put("fields", new java.util.ArrayList<Map<String,Object>>());
            byCode.put(e.getCode(), m);
        }
        for (EntityFieldRegistryEntry f : fields.findAll()) {
            Map<String, Object> entry = byCode.get(f.getEntityCode());
            if (entry == null) continue;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bucket = (List<Map<String, Object>>) entry.get("fields");
            Map<String, Object> fm = new LinkedHashMap<>();
            fm.put("fieldCode", f.getFieldCode());
            fm.put("displayLabel", f.getDisplayLabel());
            fm.put("dataType", f.getDataType());
            fm.put("filterable", f.isFilterable());
            fm.put("sortable", f.isSortable());
            fm.put("searchable", f.isSearchable());
            fm.put("required", f.isRequired());
            fm.put("relationEntity", f.getRelationEntity());
            bucket.add(fm);
        }
        return new java.util.ArrayList<>(byCode.values());
    }

    public Optional<EntityRegistryEntry> findByCode(String code) {
        return entities.findById(code);
    }

    public List<EntityFieldRegistryEntry> fieldsFor(String entityCode) {
        return fields.findAllByEntityCodeOrderByFieldCodeAsc(entityCode);
    }
}
