package com.albrimon.app.data.repository

import com.albrimon.app.data.local.entity.ScrapEntity
import com.albrimon.app.data.local.entity.UsageEntity
import com.albrimon.app.domain.model.Scrap
import com.albrimon.app.domain.model.ScrapUsage
import com.albrimon.app.data.remote.dto.ScrapDto
import com.albrimon.app.data.remote.dto.UsageDto

fun ScrapDto.toEntity() = ScrapEntity(id, name, fabricType, colorHex, widthMm, heightMm, hasGrain, photoPath, available)

fun ScrapEntity.toDomain() = Scrap(id, name, fabricType, colorHex, widthMm, heightMm, hasGrain, photoPath, available)

fun UsageDto.toEntity() = UsageEntity(id, projectId, scrapId, scrapName, usedAreaMm2, leftoverWmm, leftoverHmm, createdAt)

fun UsageEntity.toDomain() = ScrapUsage(id, projectId, scrapId, scrapName, usedAreaMm2, leftoverWmm, leftoverHmm, createdAt)
