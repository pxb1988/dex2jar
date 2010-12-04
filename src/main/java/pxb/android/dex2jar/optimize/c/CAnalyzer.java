/*
 * Copyright (c) 2009-2010 Panxiaobo
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pxb.android.dex2jar.optimize.c;

import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Interpreter;

public class CAnalyzer extends Analyzer {

	public CAnalyzer(Interpreter interpreter) {
		super(interpreter);
	}

	@Override
	protected org.objectweb.asm.tree.analysis.Frame newFrame(int nLocals, int nStack) {
		return new CFrame(nLocals, nStack);
	}

	@Override
	protected org.objectweb.asm.tree.analysis.Frame newFrame(org.objectweb.asm.tree.analysis.Frame src) {
		return new CFrame((CFrame) src);
	}
}
