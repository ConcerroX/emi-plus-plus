package concerrox.emixx.content.stackgroup.search

import concerrox.emixx.util.logError
import dev.emi.emi.api.stack.EmiIngredient
import dev.emi.emi.search.EmiSearch
import kotlin.concurrent.Volatile

object EmiPlusPlusSearch {
//    val TOKENS: Pattern = Pattern.compile("-?[@#$]?(\\/(\\\\.|[^\\\\\\/])+\\/|\\\"(\\.|[^\\\"])+\\\"|[^\\s|]+|\\||\\&)")

    @Volatile
    private var currentWorker: SearchWorker? = null

    @Volatile
    var searchThread: Thread? = null

//    @Volatile
//    var stacks: MutableList<out EmiIngredient>?
//
//    @Volatile
//    var compiledQuery: dev.emi.emi.search.EmiSearch.CompiledQuery? = null
//    var bakedStacks: MutableSet<EmiStack?>? = null
//    var names: SuffixArray<SearchStack?>? = null
//    var tooltips: SuffixArray<SearchStack?>? = null
//    var mods: SuffixArray<SearchStack?>? = null
//    var aliases: SuffixArray<EmiStack?>? = null

//    fun bake() {
//        val names: SuffixArray<SearchStack?> = SuffixArray<Any?>()
//        val tooltips: SuffixArray<SearchStack?> = SuffixArray<Any?>()
//        val mods: SuffixArray<SearchStack?> = SuffixArray<Any?>()
//        val aliases: SuffixArray<EmiStack?> = SuffixArray<Any?>()
//        val bakedStacks = Sets.newIdentityHashSet<EmiStack?>()
//        val old = EmiConfig.appendItemModId
//        EmiConfig.appendItemModId = false
//
//        for (stack in EmiStackList.stacks) {
//            try {
//                val searchStack = SearchStack(stack)
//                bakedStacks.add(stack)
//                val name = NameQuery.getText(stack)
//                if (name != null) {
//                    names.add(searchStack, name.getString().lowercase(Locale.getDefault()))
//                }
//
//                val tooltip = stack.getTooltipText()
//                if (tooltip != null) {
//                    for (i in 1..<tooltip.size) {
//                        val text = tooltip.get(i)
//                        if (text != null) {
//                            tooltips.add(searchStack, text.getString().lowercase(Locale.getDefault()))
//                        }
//                    }
//                }
//
//                val id = stack.getId()
//                if (id != null) {
//                    mods.add(searchStack, EmiUtil.getModName(id.getNamespace()).lowercase(Locale.getDefault()))
//                    mods.add(searchStack, id.getNamespace().lowercase(Locale.getDefault()))
//                    names.add(searchStack, id.getPath().lowercase(Locale.getDefault()))
//                }
//
//                if (stack.getItemStack().getItem() === Items.ENCHANTED_BOOK) {
//                    for (e in (stack.getOrDefault<ItemEnchantments?>(
//                        DataComponents.ENCHANTMENTS,
//                        ItemEnchantments.EMPTY
//                    ) as ItemEnchantments).keySet()) {
//                        val eid = EmiPort.getEnchantmentRegistry().getKey(e.value() as Enchantment)
//                        if (eid != null && eid.getNamespace() != "minecraft") {
//                            mods.add(searchStack, EmiUtil.getModName(eid.getNamespace()).lowercase(Locale.getDefault()))
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                EmiLog.error("EMI caught an exception while baking search for " + stack.toString(), e)
//            }
//        }
//
//        for (supplier in EmiData.aliases) {
//            val alias = supplier.get() as EmiAlias
//
//            for (key in alias.keys()) {
//                if (!I18n.exists(key)) {
//                    EmiReloadLog.warn("Untranslated alias " + key)
//                }
//
//                val text = I18n.get(key, *arrayOfNulls<Any>(0)).lowercase(Locale.getDefault())
//
//                for (ing in alias.stacks()) {
//                    for (stack in ing.getEmiStacks()) {
//                        aliases.add(stack.copy().comparison(EmiPort.compareStrict()), text)
//                    }
//                }
//            }
//        }
//
//        for (alias in EmiStackList.registryAliases) {
//            for (text in alias.text()) {
//                for (ing in alias.stacks()) {
//                    for (stack in ing.getEmiStacks()) {
//                        aliases.add(
//                            stack.copy().comparison(EmiPort.compareStrict()),
//                            text.getString().lowercase(Locale.getDefault())
//                        )
//                    }
//                }
//            }
//        }
//
//        EmiConfig.appendItemModId = old
//        names.generate()
//        tooltips.generate()
//        mods.generate()
//        aliases.generate()
//        dev.emi.emi.search.EmiSearch.names = names
//        dev.emi.emi.search.EmiSearch.tooltips = tooltips
//        dev.emi.emi.search.EmiSearch.mods = mods
//        dev.emi.emi.search.EmiSearch.aliases = aliases
//        dev.emi.emi.search.EmiSearch.bakedStacks = bakedStacks
//    }
//
//    fun update() {
//        dev.emi.emi.search.EmiSearch.search(EmiScreenManager.search.getValue())
//    }

    fun search(source: List<EmiIngredient>, query: String, callback: (List<EmiIngredient>) -> Unit) =
        synchronized(this.javaClass) {
            val worker = SearchWorker(query, source, callback)
            currentWorker = worker
            searchThread = Thread(worker).also {
                it.isDaemon = true
                it.start()
            }
        }

    fun apply(worker: SearchWorker, stacks: List<EmiIngredient>, callback: (List<EmiIngredient>) -> Unit) {
        synchronized(this.javaClass) {
            if (worker === currentWorker) {
//                stacks = stacks
                callback(stacks)
                currentWorker = null
                searchThread = null
            }
        }
    }

    init {
//        stacks = EmiStackList.stacks
    }

//    class CompiledQuery(query: String) {
//        val fullQuery: Query?
//
//        init {
//            val full: MutableList<Query?> = Lists.newArrayList<Query?>()
//            var queries: MutableList<Query?> = Lists.newArrayList<Query?>()
//            val matcher = dev.emi.emi.search.EmiSearch.TOKENS.matcher(query)
//
//            while (matcher.find()) {
//                var q = matcher.group()
//                val negated = q.startsWith("-")
//                if (negated) {
//                    q = q.substring(1)
//                }
//
//                if (!q.isEmpty() && q != "&") {
//                    if (q == "|") {
//                        if (!queries.isEmpty()) {
//                            full.add(LogicalAndQuery(queries))
//                            queries = Lists.newArrayList<Query?>()
//                        }
//                    } else {
//                        val type = QueryType.fromString(q)
//                        var constructor = type.queryConstructor
//                        var regexConstructor = type.regexQueryConstructor
//                        if (type == QueryType.DEFAULT) {
//                            val constructors: MutableList<Function<String?, Query?>?> =
//                                Lists.newArrayList<Function<String?, Query?>?>()
//                            val regexConstructors: MutableList<Function<String?, Query?>?> =
//                                Lists.newArrayList<Function<String?, Query?>?>()
//                            constructors.add(constructor)
//                            regexConstructors.add(regexConstructor)
//                            if (EmiConfig.searchTooltipByDefault) {
//                                constructors.add(QueryType.TOOLTIP.queryConstructor)
//                                regexConstructors.add(QueryType.TOOLTIP.regexQueryConstructor)
//                            }
//
//                            if (EmiConfig.searchModNameByDefault) {
//                                constructors.add(QueryType.MOD.queryConstructor)
//                                regexConstructors.add(QueryType.MOD.regexQueryConstructor)
//                            }
//
//                            if (EmiConfig.searchTagsByDefault) {
//                                constructors.add(QueryType.TAG.queryConstructor)
//                                regexConstructors.add(QueryType.TAG.regexQueryConstructor)
//                            }
//
//                            constructors.add(Function { name: String? -> AliasQuery(name) })
//                            if (constructors.size > 1) {
//                                constructor = Function { name: String? ->
//                                    LogicalOrQuery(
//                                        constructors.stream()
//                                            .map<Query?> { c: Function<String?, Query?>? -> c!!.apply(name) }.toList()
//                                    )
//                                }
//                                regexConstructor = Function { name: String? ->
//                                    LogicalOrQuery(
//                                        regexConstructors.stream()
//                                            .map<Query?> { c: Function<String?, Query?>? -> c!!.apply(name) }.toList()
//                                    )
//                                }
//                            }
//                        }
//
//                        dev.emi.emi.search.EmiSearch.CompiledQuery.Companion.addQuery(
//                            q.substring(type.prefix.length),
//                            negated,
//                            queries,
//                            constructor,
//                            regexConstructor
//                        )
//                    }
//                }
//            }
//
//            if (!queries.isEmpty()) {
//                full.add(LogicalAndQuery(queries))
//            }
//
//            if (!full.isEmpty()) {
//                this.fullQuery = LogicalOrQuery(full)
//            } else {
//                this.fullQuery = null
//            }
//        }
//
//        val isEmpty: Boolean
//            get() = this.fullQuery == null
//
//        fun test(stack: EmiStack?): Boolean {
//            if (this.fullQuery == null) {
//                return true
//            } else {
//                return if (dev.emi.emi.search.EmiSearch.bakedStacks.contains(stack)) this.fullQuery.matches(stack) else this.fullQuery.matchesUnbaked(
//                    stack
//                )
//            }
//        }
//
//        companion object {
//            private fun addQuery(
//                s: String,
//                negated: Boolean,
//                queries: MutableList<Query?>,
//                normal: Function<String?, Query?>,
//                regex: Function<String?, Query?>
//            ) {
//                val q: Query
//                if (s.length > 1 && s.startsWith("/") && s.endsWith("/")) {
//                    q = regex.apply(s.substring(1, s.length - 1)) as Query
//                } else if (s.length > 1 && s.startsWith("\"") && s.endsWith("\"")) {
//                    q = normal.apply(s.substring(1, s.length - 1)) as Query
//                } else {
//                    q = normal.apply(s) as Query
//                }
//
//                q.negated = negated
//                queries.add(q)
//            }
//        }
//    }

    class SearchWorker(
        private val query: String,
        private val source: List<EmiIngredient>,
        private val callback: (List<EmiIngredient>) -> Unit
    ) : Runnable {

        override fun run() {
            try {
                val compiled = EmiSearch.CompiledQuery(query)
//                EmiPlusPlusSearch.compiledQuery = compiled

                if (compiled.isEmpty) {
                    apply(this, source, callback)
                    return
                }

                val stacks = mutableListOf<EmiIngredient>()
                var processed = 0

                for (stack in source) {
                    if (processed++ >= 1024) {
                        processed = 0
                        if (this !== currentWorker) return
                    }

                    val emiStacks = stack.emiStacks
                    if (emiStacks.size == 1) {
                        val emiStack = emiStacks[0]
                        if (compiled.test(emiStack)) stacks.add(stack)
                    }
                }

                apply(this, stacks.toList(), callback)
            } catch (e: Exception) {
                logError("Error when attempting to search:", e)
            }
        }

    }
}