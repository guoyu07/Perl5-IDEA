/*
 * Copyright 2015 Alexandr Evstigneev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.perl5.lang.mason.elementType;

import com.intellij.psi.stubs.StubElement;
import com.perl5.lang.mason.MasonPerlLanguage;
import com.perl5.lang.mason.psi.impl.MasonNamespaceDefinitionImpl;
import com.perl5.lang.perl.idea.stubs.namespaces.PerlNamespaceDefinitionStub;
import com.perl5.lang.perl.idea.stubs.namespaces.PerlNamespaceDefinitionStubElementType;
import com.perl5.lang.perl.idea.stubs.namespaces.PerlNamespaceDefinitionStubImpl;
import com.perl5.lang.perl.psi.PerlNamespaceDefinition;
import org.jetbrains.annotations.NotNull;

/**
 * Created by hurricup on 05.01.2016.
 */
public class MasonNamespaceElementType extends PerlNamespaceDefinitionStubElementType
{
	public MasonNamespaceElementType(String name)
	{
		super(name, MasonPerlLanguage.INSTANCE);
	}

	@Override
	public PerlNamespaceDefinition createPsi(@NotNull PerlNamespaceDefinitionStub stub)
	{
		return new MasonNamespaceDefinitionImpl(stub, this);
	}

	@Override
	public PerlNamespaceDefinitionStub createStub(@NotNull PerlNamespaceDefinition psi, StubElement parentStub)
	{
		assert psi instanceof MasonNamespaceDefinitionImpl;
		return new PerlNamespaceDefinitionStubImpl(
				parentStub,
				this,
				psi.getPackageName(),
				psi.getMroType(),
				((MasonNamespaceDefinitionImpl) psi).getParentNamespacesFromPsi(),
				psi.isDeprecated(),
				psi.getEXPORT(),
				psi.getEXPORT_OK(),
				psi.getEXPORT_TAGS()
		);
	}
}