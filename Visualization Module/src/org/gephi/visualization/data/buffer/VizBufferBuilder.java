/*
Copyright 2008-2011 Gephi
Authors : Antonio Patriarca <antoniopatriarca@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gephi.visualization.data.buffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import org.gephi.visualization.data.layout.VizLayout;

/**
 * Class used to create a new VizBuffer object.
 *
 * Antonio Patriarca <antoniopatriarca@gmail.com>
 */
public class VizBufferBuilder<T> {

    private final VizLayout<T> layout;
    private ArrayList<ByteBuffer> buffers;
    private int currentBuffer;

    public VizBufferBuilder(VizLayout<T> layout) {
        this.layout = layout;
        this.buffers = new ArrayList<ByteBuffer>();
        this.currentBuffer = 0;
    }

    public VizBufferBuilder(VizBuffer<T> oldBuffer) {
        this(oldBuffer.layout);
        recycle(oldBuffer);
    }

    public final void recycle(VizBuffer<T> oldBuffer) {
        if (this.layout != oldBuffer.layout) return;

        for (ByteBuffer b : oldBuffer.buffers) {
            b.position(0);
            b.limit(b.capacity());
            this.buffers.add(b);
        }
    }

    public final VizLayout<T> layout() {
        return this.layout;
    }

    public VizBuffer<T> createVizBuffer() {
        if (this.buffers.isEmpty()) {
            return new VizBuffer<T>(this.layout, new ArrayList<ByteBuffer>());
        }

        ArrayList<ByteBuffer> oldBuffers = new ArrayList<ByteBuffer>(this.currentBuffer + 1);
        ArrayList<ByteBuffer> newBuffers = new ArrayList<ByteBuffer>(this.buffers.size() - this.currentBuffer + 1);

        int i;
        for (i = 0; i <= this.currentBuffer; ++i) {
            ByteBuffer b = this.buffers.get(i);
            b.flip();
            oldBuffers.add(b);
        }

        for (; i < this.buffers.size(); ++i) {
            newBuffers.add(this.buffers.get(i));
        }

        this.buffers = newBuffers;
        this.currentBuffer = 0;

        return new VizBuffer<T>(this.layout, Collections.unmodifiableList(oldBuffers));
    }

    public void add(T e) {
        if (this.buffers.isEmpty()) {
            final ByteBuffer b = ByteBuffer.allocateDirect(this.layout.suggestedBlockSize());
            this.buffers.add(b);
            this.currentBuffer = 0;
        }
        
        ByteBuffer b = this.buffers.get(this.currentBuffer);
        
        if (!this.layout.add(b, e)) {
            ++this.currentBuffer;
            if (this.currentBuffer < this.buffers.size()) {
                b = this.buffers.get(this.currentBuffer);
            } else {
                b = ByteBuffer.allocateDirect(this.layout.suggestedBlockSize());
                this.buffers.add(b);
            }
            this.layout.add(b, e); // it should always succeed
        }
    }
}