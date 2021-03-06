/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.backup.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class StrategyResolverService
{
    private final BackupStrategyWrapper haBackupStrategy;
    private final BackupStrategyWrapper ccBackupStrategy;

    StrategyResolverService( BackupStrategyWrapper haBackupStrategy, BackupStrategyWrapper ccBackupStrategy )
    {
        this.haBackupStrategy = haBackupStrategy;
        this.ccBackupStrategy = ccBackupStrategy;
    }

    List<BackupStrategyWrapper> getStrategies( SelectedBackupProtocol selectedBackupProtocol )
    {
        switch ( selectedBackupProtocol )
        {
        case ANY:
            return Arrays.asList( ccBackupStrategy, haBackupStrategy );
        case COMMON:
            return Collections.singletonList( haBackupStrategy );
        case CATCHUP:
            return Collections.singletonList( ccBackupStrategy );
        default:
            throw new IllegalArgumentException( "Unhandled protocol choice: " + selectedBackupProtocol );
        }
    }
}
