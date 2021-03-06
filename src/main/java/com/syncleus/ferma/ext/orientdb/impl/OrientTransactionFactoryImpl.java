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
package com.syncleus.ferma.ext.orientdb.impl;

import java.util.Set;

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;

import com.syncleus.ferma.EdgeFrame;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.ext.orientdb.DelegatingFramedOrientGraph;
import com.syncleus.ferma.ext.orientdb.OrientDBTx;
import com.syncleus.ferma.ext.orientdb.OrientDBTypeResolver;
import com.syncleus.ferma.ext.orientdb.OrientTransactionFactory;
import com.syncleus.ferma.framefactories.DefaultFrameFactory;
import com.syncleus.ferma.framefactories.FrameFactory;
import com.syncleus.ferma.framefactories.annotation.AnnotationFrameFactory;
import com.syncleus.ferma.tx.Tx;

public class OrientTransactionFactoryImpl implements OrientTransactionFactory {

	protected OrientGraphFactory factory;

	private FrameFactory frameFactory;

	private OrientDBTypeResolver typeResolver;

	/**
	 * Create a new orientdb transaction factory.
	 * 
	 * @param factory
	 * @param annotationsSupported
	 *            True if annotated classes will be supported, false otherwise.
	 * @param basePaths
	 * 
	 */
	public OrientTransactionFactoryImpl(OrientGraphFactory factory, boolean annotationsSupported, String... basePaths) {
		this.factory = factory;
		this.typeResolver = new OrientDBTypeResolver(basePaths);

		if (annotationsSupported) {
			final ReflectionCache reflections = new ReflectionCache();
			this.frameFactory = new AnnotationFrameFactory(reflections);
		} else {
			this.frameFactory = new DefaultFrameFactory();
		}
	}

	@Override
	public Tx tx() {
		Tx tx = Tx.getActive();
		if (tx != null) {
			return new OrientDBTx(tx);
		} else {
			tx = createTx();
			Tx.setActive(tx);
			return tx;
		}
	}

	@Override
	public OrientGraphFactory getFactory() {
		return factory;
	}

	@Override
	public OrientDBTypeResolver getTypeResolver() {
		return typeResolver;
	}

	@Override
	public FrameFactory getFrameFactory() {
		return frameFactory;
	}

	@Override
	public void setFrameFactory(FrameFactory frameFactory) {
		this.frameFactory = frameFactory;
	}

	@Override
	public void setupElementClasses() {
		Set<Class<?>> classes = typeResolver.getGraphElementClasses();
		for (Class<?> clazz : classes) {
			if (VertexFrame.class.isAssignableFrom(clazz)) {
				addVertexClass(clazz.getSimpleName(), "V");
			}
			if (EdgeFrame.class.isAssignableFrom(clazz)) {
				addEdgeClass(clazz.getSimpleName());
			}
		}
	}

	@Override
	public void addEdgeClass(String label) {
		OrientGraph noTx = factory.getNoTx();
		try {
			noTx.createEdgeClass(label);
			noTx.commit();
		} finally {
			noTx.close();
		}
	}

	@Override
	public void addVertexClass(String className, String superClassName) {
		OrientGraph noTx = factory.getNoTx();
		try {
			noTx.createClass(className, superClassName);
			noTx.commit();
		} finally {
			noTx.close();
		}
	}

	/**
	 * Return the maximum count a transaction should be repeated if a retry is needed.
	 * 
	 * @return
	 */
	public int getMaxRetry() {
		return 20;
	}

	@Override
	public Tx createTx() {
		OrientGraph rawTx = getFactory().getTx();
		DelegatingFramedOrientGraph framedGraph = new DelegatingFramedOrientGraph(rawTx, getFrameFactory(), getTypeResolver());
		OrientDBTx tx = new OrientDBTx(rawTx.tx(), framedGraph);
		return tx;
	}

}
