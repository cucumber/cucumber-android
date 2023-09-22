package io.cucumber.android

import android.content.Context
import android.content.res.AssetManager
import io.cucumber.core.feature.FeatureIdentifier
import io.cucumber.core.feature.FeatureParser
import io.cucumber.core.feature.Options
import io.cucumber.core.gherkin.Feature
import io.cucumber.core.resource.Resource
import io.cucumber.core.runtime.FeatureSupplier
import java.io.InputStream
import java.net.URI
import kotlin.jvm.optionals.getOrNull

internal class AndroidFeatureSupplier(
    private val featureOptions: Options,
    private val parser: FeatureParser,
    private val context: Context
) : FeatureSupplier {

    companion object {
        private const val RESOURCE_PATH_FORMAT = "%s/%s"
    }
    override fun get(): List<Feature> {
        val resources = ArrayList<Resource>()
        val assetManager = context.assets

        featureOptions.featurePaths.forEach {
            addResourceRecursive(resources, assetManager, it)
        }
        return resources.mapNotNull { parser.parseResource(it).getOrNull() }
    }

    private fun addResourceRecursive(
        resources: MutableList<Resource>,
        assetManager: AssetManager,
        path: URI,
    ) {
        if (FeatureIdentifier.isFeature(path)) {
            resources.add(AndroidResource(context, path))
            return
        }
        val schemeSpecificPart = path.pathInAssets()
        val list = assetManager.list(schemeSpecificPart)
        if (list != null) {
            for (name in list) {
                val subPath: String = String.format(RESOURCE_PATH_FORMAT, path.toString(), name)
                addResourceRecursive(resources, assetManager, URI.create(subPath))
            }
        }
    }

    /**
     * Android specific implementation of [Resource] which is apple
     * to create [InputStream]s for android assets.
     */
    private class AndroidResource(
        /**
         * The [Context] to get the [InputStream] from
         */
        private val context: Context,
        /**
         * The path of the resource.
         */
        val path: URI
    ) : Resource {

        private val pathInAssets: String = path.pathInAssets()

        override fun getUri(): URI = path

        override fun getInputStream(): InputStream {
            return context.assets.open(pathInAssets, AssetManager.ACCESS_UNKNOWN)
        }

        override fun toString(): String {
            return "AndroidResource ($pathInAssets)"
        }
    }

}

private fun URI.pathInAssets() = schemeSpecificPart.removePrefix("assets:").removePrefix("/")
