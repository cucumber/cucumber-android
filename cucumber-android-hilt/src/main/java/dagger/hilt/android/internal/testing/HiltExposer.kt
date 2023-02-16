package dagger.hilt.android.internal.testing

internal object HiltExposer {


    fun getTestComponentData(clazz:Class<*>): TestComponentData? {
        return runCatching { TestComponentDataSupplier.get(clazz) }.getOrNull()
    }
}