/*
 *
 *  Copyright 2017 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.genie.web.jpa.repositories;

import com.netflix.genie.web.jpa.entities.FileEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository for file references.
 *
 * @author tgianos
 * @since 3.3.0
 */
public interface JpaFileRepository extends JpaIdRepository<FileEntity> {

    /**
     * The query used to delete any dangling file references.
     */
    String DELETE_UNUSED_FILES_SQL =
        "DELETE "
            + "FROM files "
            + "WHERE id NOT IN (SELECT DISTINCT(setup_file) FROM applications WHERE setup_file IS NOT NULL) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM applications_configs) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM applications_dependencies) "
            + "AND id NOT IN (SELECT DISTINCT(setup_file) FROM clusters WHERE setup_file IS NOT NULL) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM clusters_configs) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM clusters_dependencies) "
            + "AND id NOT IN (SELECT DISTINCT(setup_file) FROM commands WHERE setup_file IS NOT NULL) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM commands_configs) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM commands_dependencies) "
            + "AND id NOT IN (SELECT DISTINCT(setup_file) FROM jobs WHERE setup_file IS NOT NULL) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM jobs_configs) "
            + "AND id NOT IN (SELECT DISTINCT(file_id) FROM jobs_dependencies) "
            + "AND created <= :createdThreshold ;";

    /**
     * Find a file by its unique file value.
     *
     * @param file The file value to search for
     * @return An Optional of a FileEntity
     */
    Optional<FileEntity> findByFile(final String file);

    /**
     * Find out whether a file entity with the given file value exists.
     *
     * @param file The file value to check for
     * @return True if the file exists
     */
    boolean existsByFile(final String file);

    /**
     * Delete all files from the database that aren't referenced which were created before the supplied created
     * threshold.
     *
     * @param createdThreshold The instant in time where files created before this time that aren't referenced
     *                         will be deleted. Inclusive.
     * @return The number of files deleted
     */
    @Modifying
    @Query(value = DELETE_UNUSED_FILES_SQL, nativeQuery = true)
    int deleteUnusedFiles(@Param("createdThreshold") final Instant createdThreshold);
}