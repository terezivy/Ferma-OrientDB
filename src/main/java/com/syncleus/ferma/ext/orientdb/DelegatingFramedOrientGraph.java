/**
 * Copyright 2004 - 2017 Syncleus, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * This product currently only contains code developed by authors
 * of specific components, as identified by the source code files.
 *
 * Since product implements StAX API, it has dependencies to StAX API
 * classes.
 *
 * For additional credits (generally to people who reported problems)
 * see CREDITS file.
 */
package com.syncleus.ferma.ext.orientdb;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;

import com.syncleus.ferma.ClassInitializer;
import com.syncleus.ferma.DefaultClassInitializer;
import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.framefactories.FrameFactory;
import com.syncleus.ferma.tx.Tx;
import com.syncleus.ferma.tx.WrappedFramedTxGraph;
import com.syncleus.ferma.typeresolvers.TypeResolver;


public class DelegatingFramedOrientGraph extends DelegatingFramedGraph<OrientGraph> implements WrappedFramedTxGraph<OrientGraph> {

	public DelegatingFramedOrientGraph(OrientGraph delegate, FrameFactory factory, TypeResolver typeResolver) {
		super(delegate, factory, typeResolver);
	}

	@Override
	public <T> T addFramedVertex(final ClassInitializer<T> initializer, Object... keyValues) {
		return frameNewElement(this.getBaseGraph().addVertex(keyValues), initializer);
	}

	@Override
	public <T> T addFramedEdge(VertexFrame source, VertexFrame destination, String label, ClassInitializer<T> initializer, Object... keyValues) {
		return frameNewElement(source.addFramedEdge(label, destination).getElement(), initializer);
	}

	@Override
	public <T> T addFramedVertex(final Class<T> kind) {
		return this.addFramedVertex(new DefaultClassInitializer<>(kind), org.apache.tinkerpop.gremlin.structure.T.label, kind.getSimpleName());
	}

	@Override
	public <T> T addFramedEdge(VertexFrame source, VertexFrame destination, String label, Class<T> kind) {
		return super.addFramedEdge(source, destination, label, kind);
	}

	@Override
	public Tx createTx() {
		return new DelegatingFramedOrientTransaction(this.getBaseGraph().tx(), this);
	}

	@Override
	public Tx tx() {
		return WrappedFramedTxGraph.super.tx();
	}

}
