package com.aria.entity_registry;

import com.aria.entity_registry.dao.EntityFieldRegistryRepository;
import com.aria.entity_registry.dao.EntityRegistryRepository;
import com.aria.entity_registry.model.EntityFieldRegistryEntry;
import com.aria.entity_registry.model.EntityRegistryEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/** Unit test for {@link EntityRegistryService}: nest fields under entities. */
class EntityRegistryServiceTest {

    private EntityRegistryRepository entities;
    private EntityFieldRegistryRepository fields;
    private EntityRegistryService svc;

    @BeforeEach
    void setUp() {
        entities = mock(EntityRegistryRepository.class);
        fields = mock(EntityFieldRegistryRepository.class);
        svc = new EntityRegistryService();
        ReflectionTestUtils.setField(svc, "entities", entities);
        ReflectionTestUtils.setField(svc, "fields", fields);
    }

    @Test
    void listAllWithFields_nestsFieldsUnderEntity() {
        when(entities.findAllByOrderByCodeAsc()).thenReturn(List.of(
                EntityRegistryEntry.builder().code("Vendor").displayName("Vendor")
                        .tableName("m0_vendor").category("M3").build(),
                EntityRegistryEntry.builder().code("City").displayName("City")
                        .tableName("city").category("COMMON").build()));
        when(fields.findAll()).thenReturn(List.of(
                EntityFieldRegistryEntry.builder().entityCode("Vendor").fieldCode("name")
                        .displayLabel("Name").dataType("STRING").filterable(true)
                        .sortable(true).searchable(true).required(true).build(),
                EntityFieldRegistryEntry.builder().entityCode("Vendor").fieldCode("gstin")
                        .displayLabel("GSTIN").dataType("STRING").build(),
                EntityFieldRegistryEntry.builder().entityCode("City").fieldCode("name")
                        .displayLabel("City Name").dataType("STRING").build()));

        List<Map<String, Object>> rows = svc.listAllWithFields();
        assertThat(rows).hasSize(2);
        assertThat(rows.get(0)).containsEntry("code", "Vendor");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> vendorFields = (List<Map<String, Object>>) rows.get(0).get("fields");
        assertThat(vendorFields).hasSize(2);
        assertThat(vendorFields.get(0)).containsEntry("fieldCode", "name");
        assertThat(vendorFields.get(0)).containsEntry("filterable", true);
    }

    @Test
    void findByCode_delegates() {
        when(entities.findById("Vendor")).thenReturn(Optional.of(
                EntityRegistryEntry.builder().code("Vendor").build()));
        assertThat(svc.findByCode("Vendor")).isPresent();
        assertThat(svc.findByCode("Vendor").get().getCode()).isEqualTo("Vendor");
    }

    @Test
    void fieldsFor_delegates() {
        when(fields.findAllByEntityCodeOrderByFieldCodeAsc("Vendor")).thenReturn(List.of(
                EntityFieldRegistryEntry.builder().entityCode("Vendor").fieldCode("name").build()));
        assertThat(svc.fieldsFor("Vendor")).hasSize(1);
    }
}
