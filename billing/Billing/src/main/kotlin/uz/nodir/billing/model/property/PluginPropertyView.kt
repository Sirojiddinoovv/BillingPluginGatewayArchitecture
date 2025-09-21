package uz.nodir.billing.model.property

import uz.nodir.paycommon.enums.Processor


/**
@author: Nodir
@date: 22.09.2025
@group: Meloman

 **/

interface PluginPropertyView {
    val dir: String
    val watch: Boolean
    val adapters: PluginProperty.Adapters

    fun isEnabled(id: Processor): Boolean
}