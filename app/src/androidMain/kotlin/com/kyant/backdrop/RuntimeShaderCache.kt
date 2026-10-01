/*
   Copyright 2025 Kyant

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

   Ported into Nanxin Shiguang (NUIST edition) from
   https://github.com/Kyant0/AndroidLiquidGlass
   (io.github.kyant0:backdrop, io.github.kyant0:shapes).
   See the repository NOTICE file.
 */

package com.kyant.backdrop

import org.intellij.lang.annotations.Language

sealed interface RuntimeShaderCache {

    fun obtainRuntimeShader(key: String, @Language("AGSL") string: String): RuntimeShader
}

internal class RuntimeShaderCacheImpl : RuntimeShaderCache {

    override fun obtainRuntimeShader(key: String, string: String): RuntimeShader {
        return ShaderRegistry.runtimeShaders.getOrPut(key) { RuntimeShader(string) }
    }

    fun clear() {
        // C2：lens 的 AGSL shader 全局共享编译，detach 时不再清除，
        // 让各卡片复用同一份已编译的程序对象，避免每张卡重复编译。
        // key 与 shader 字符串一一对应（静态常量），可常驻缓存。
    }
}

// C2 优化：进程级共享的编译程序池。
// 各卡片仅修改自己 RenderEffect 里的 uniform（先 set 再 createRuntimeShaderEffect），
// 共享同一 android.graphics.RuntimeShader 程序本身不互相污染。
private object ShaderRegistry {
    val runtimeShaders = mutableMapOf<String, RuntimeShader>()
}
