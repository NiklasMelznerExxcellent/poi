/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hpsf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;

/**
 * Factory class to create instances of {@link SummaryInformation},
 * {@link DocumentSummaryInformation} and {@link PropertySet}.
 */
public class PropertySetFactory {
    /**
     * Creates the most specific {@link PropertySet} from an entry
     *  in the specified POIFS Directory. This is preferrably a {@link
     * DocumentSummaryInformation} or a {@link SummaryInformation}. If
     * the specified entry does not contain a property set stream, an 
     * exception is thrown. If no entry is found with the given name,
     * an exception is thrown.
     *
     * @param dir The directory to find the PropertySet in
     * @param name The name of the entry containing the PropertySet
     * @return The created {@link PropertySet}.
     * @throws FileNotFoundException if there is no entry with that name
     * @throws NoPropertySetStreamException if the stream does not
     * contain a property set.
     * @throws IOException if some I/O problem occurs.
     * @exception UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    public static PropertySet create(final DirectoryEntry dir, final String name)
    throws FileNotFoundException, NoPropertySetStreamException, IOException, UnsupportedEncodingException {
        InputStream inp = null;
        try {
            DocumentEntry entry = (DocumentEntry)dir.getEntry(name);
            inp = new DocumentInputStream(entry);
            try {
                return create(inp);
            } catch (MarkUnsupportedException e) {
                return null;
            }
        } finally {
            if (inp != null) {
                inp.close();
            }
        }
    }

    /**
     * Creates the most specific {@link PropertySet} from an {@link
     * InputStream}. This is preferrably a {@link
     * DocumentSummaryInformation} or a {@link SummaryInformation}. If
     * the specified {@link InputStream} does not contain a property
     * set stream, an exception is thrown and the {@link InputStream}
     * is repositioned at its beginning.
     *
     * @param stream Contains the property set stream's data.
     * @return The created {@link PropertySet}.
     * @throws NoPropertySetStreamException if the stream does not
     * contain a property set.
     * @throws MarkUnsupportedException if the stream does not support
     * the {@code mark} operation.
     * @throws IOException if some I/O problem occurs.
     * @exception UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    public static PropertySet create(final InputStream stream)
    throws NoPropertySetStreamException, MarkUnsupportedException, UnsupportedEncodingException, IOException {
        final PropertySet ps = new PropertySet(stream);
        try {
            if (ps.isSummaryInformation()) {
                return new SummaryInformation(ps);
            } else if (ps.isDocumentSummaryInformation()) {
                return new DocumentSummaryInformation(ps);
            } else {
                return ps;
            }
        } catch (UnexpectedPropertySetTypeException ex) {
            /* This exception will never be throws because we already checked
             * explicitly for this case above. */
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Creates a new summary information.
     *
     * @return the new summary information.
     */
    public static SummaryInformation newSummaryInformation() {
        return new SummaryInformation();
    }

    /**
     * Creates a new document summary information.
     *
     * @return the new document summary information.
     */
    public static DocumentSummaryInformation newDocumentSummaryInformation() {
        return new DocumentSummaryInformation();
    }
}