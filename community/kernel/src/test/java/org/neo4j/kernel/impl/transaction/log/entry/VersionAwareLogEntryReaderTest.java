/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.kernel.impl.transaction.log.entry;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import org.neo4j.kernel.impl.api.TestCommand;
import org.neo4j.kernel.impl.api.TestCommandReaderFactory;
import org.neo4j.kernel.impl.transaction.log.InMemoryClosableChannel;
import org.neo4j.kernel.impl.transaction.log.LogPosition;
import org.neo4j.storageengine.api.CommandReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VersionAwareLogEntryReaderTest
{
    private final LogEntryReader logEntryReader = new VersionAwareLogEntryReader( new TestCommandReaderFactory() );

    @Test
    void shouldReadAStartLogEntry() throws IOException
    {
        // given
        LogEntryVersion version = LogEntryVersion.LATEST_VERSION;
        final LogEntryStart start = new LogEntryStart( version, 1, 2, 3, 4, new byte[]{5}, new LogPosition( 0, 31 ) );
        final InMemoryClosableChannel channel = new InMemoryClosableChannel();

        channel.put( version.version() ); // version
        channel.put( LogEntryByteCodes.TX_START ); // type
        channel.putInt( start.getMasterId() );
        channel.putInt( start.getLocalId() );
        channel.putLong( start.getTimeWritten() );
        channel.putLong( start.getLastCommittedTxWhenTransactionStarted() );
        channel.putInt( start.getAdditionalHeader().length );
        channel.put( start.getAdditionalHeader(), start.getAdditionalHeader().length );

        // when
        final LogEntry logEntry = logEntryReader.readLogEntry( channel );

        // then
        assertEquals( start, logEntry );
    }

    @Test
    void shouldReadACommitLogEntry() throws IOException
    {
        // given
        LogEntryVersion version = LogEntryVersion.LATEST_VERSION;
        final LogEntryCommit commit = new LogEntryCommit( version, 42, 21 );
        final InMemoryClosableChannel channel = new InMemoryClosableChannel();

        channel.put( version.version() );
        channel.put( LogEntryByteCodes.TX_COMMIT );
        channel.putLong( commit.getTxId() );
        channel.putLong( commit.getTimeWritten() );

        // when
        final LogEntry logEntry = logEntryReader.readLogEntry( channel );

        // then
        assertEquals( commit, logEntry );
    }

    @Test
    void shouldReadACommandLogEntry() throws IOException
    {
        // given
        LogEntryVersion version = LogEntryVersion.LATEST_VERSION;
        TestCommand testCommand = new TestCommand( new byte[] {100, 101, 102} );
        final LogEntryCommand command = new LogEntryCommand( version, testCommand );
        final InMemoryClosableChannel channel = new InMemoryClosableChannel();

        channel.put( version.version() );
        channel.put( LogEntryByteCodes.COMMAND );
        testCommand.serialize( channel );

        // when
        final LogEntry logEntry = logEntryReader.readLogEntry( channel );

        // then
        assertEquals( command, logEntry );
    }

    @Test
    void shouldReadACheckPointLogEntry() throws IOException
    {
        // given
        LogEntryVersion version = LogEntryVersion.LATEST_VERSION;
        final LogPosition logPosition = new LogPosition( 42, 43 );
        final CheckPoint checkPoint = new CheckPoint( version, logPosition );
        final InMemoryClosableChannel channel = new InMemoryClosableChannel();

        channel.put( version.version() );
        channel.put( LogEntryByteCodes.CHECK_POINT );
        channel.putLong( logPosition.getLogVersion() );
        channel.putLong( logPosition.getByteOffset() );

        // when
        final LogEntry logEntry = logEntryReader.readLogEntry( channel );

        // then
        assertEquals( checkPoint, logEntry );
    }

    @Test
    void shouldReturnNullWhenThereIsNoCommand() throws IOException
    {
        // given
        LogEntryVersion version = LogEntryVersion.LATEST_VERSION;
        final InMemoryClosableChannel channel = new InMemoryClosableChannel();

        channel.put( version.version() );
        channel.put( LogEntryByteCodes.COMMAND );
        channel.put( CommandReader.NONE );

        // when
        final LogEntry logEntry = logEntryReader.readLogEntry( channel );

        // then
        assertNull( logEntry );
    }

    @Test
    void shouldReturnNullWhenNotEnoughDataInTheChannel() throws IOException
    {
        // given
        final InMemoryClosableChannel channel = new InMemoryClosableChannel();

        // when
        final LogEntry logEntry = logEntryReader.readLogEntry( channel );

        // then
        assertNull( logEntry );
    }
}
