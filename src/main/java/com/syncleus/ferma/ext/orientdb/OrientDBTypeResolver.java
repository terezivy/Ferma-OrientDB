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

import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.tinkerpop.gremlin.orientdb.OrientEdge;
import org.apache.tinkerpop.gremlin.orientdb.OrientVertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.util.wrapped.WrappedElement;
import org.reflections.Reflections;

import com.syncleus.ferma.AbstractEdgeFrame;
import com.syncleus.ferma.AbstractVertexFrame;
import com.syncleus.ferma.EdgeFrame;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.annotations.GraphElement;
import com.syncleus.ferma.ext.ElementTypeClassCache;
import com.syncleus.ferma.typeresolvers.TypeResolver;

public class OrientDBTypeResolver implements TypeResolver {

	private final ElementTypeClassCache elementTypCache;

	private String[] basePath;

	public OrientDBTypeResolver(String... basePath) {
		this.basePath = basePath;
		this.elementTypCache = new ElementTypeClassCache(basePath);
	}

	@Override
	public <T> Class<? extends T> resolve(Element element, Class<T> kind) {
		if (element instanceof WrappedElement) {
			element = ((WrappedElement<Element>) element).getBaseElement();
		}
		if (element instanceof OrientVertex) {
			OrientVertex orientVertex = (OrientVertex) element;
			String name = orientVertex.label();
			return resolve(name, kind);
		}
		if (element instanceof OrientEdge) {
			OrientEdge orientEdge = (OrientEdge) element;
			String name = orientEdge.getRawElement().getSchemaType().get().getSuperClass().getName();
			return resolve(name, kind);
		}
		return null;
	}

	private <T> Class<? extends T> resolve(String type, Class<T> kind) {
		final Class<T> nodeKind = (Class<T>) this.elementTypCache.forName(type);
		if (kind.isAssignableFrom(nodeKind) || kind.equals(VertexFrame.class) || kind.equals(EdgeFrame.class)
				|| kind.equals(AbstractVertexFrame.class) || kind.equals(AbstractEdgeFrame.class) || kind.equals(Object.class)) {
			return nodeKind;
		} else {
			return kind;
		}
	}

	@Override
	public Class<?> resolve(Element element) {
		if (element instanceof OrientVertex) {
			OrientVertex orientVertex = (OrientVertex) element;
			String name = orientVertex.label();
			return this.elementTypCache.forName(name);
		}
		if (element instanceof OrientEdge) {
			OrientEdge orientEdge = (OrientEdge) element;
			String name = orientEdge.label();
			return this.elementTypCache.forName(name);
		}
		return null;
	}

	@Override
	public void init(Element element, Class<?> kind) {
		// NOOP
	}

	@Override
	public void deinit(Element element) {
		throw new NotImplementedException("DeInit is not yet supported.");
	}

	@Override
	public <P extends Element, T extends Element> GraphTraversal<P, T> hasType(GraphTraversal<P, T> traverser, Class<?> type) {
		return traverser.filter(vertex -> {
			Class<?> vertexType = resolve(vertex.get());
			return vertexType == type;
		});
	}

	@Override
	public <P extends Element, T extends Element> GraphTraversal<P, T> hasNotType(GraphTraversal<P, T> traverser, Class<?> type) {
		return traverser.filter(vertex -> {
			Class<?> vertexType = resolve(vertex.get());
			return vertexType == type;
		});
	}

	/**
	 * Return all found graph element classes.
	 * 
	 * @return
	 */
	public Set<Class<?>> getGraphElementClasses() {
		Set<Class<?>> graphTypeClasses = new Reflections(basePath).getTypesAnnotatedWith(GraphElement.class);
		return graphTypeClasses;
	}

}
