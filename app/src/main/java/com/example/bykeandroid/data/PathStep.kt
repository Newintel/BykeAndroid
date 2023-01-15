package com.example.bykeandroid.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
@kotlinx.serialization.Serializable
class PathStepId(
    val pathId: Int,
    val position: Int,
) : Parcelable

@Parcelize
@kotlinx.serialization.Serializable
class PathStep(
    val id: PathStepId? = null,
    val step: Step? = null,
) : java.io.Serializable, Parcelable