package com.itami.gmdprogresses.autofuncs

import android.content.Context
import com.itami.gmdprogresses.manylist.ExtremeDemonEntity

class whatorganizelist {
    fun parseDemonsFromAssets(context: Context): List<ExtremeDemonEntity> {
        val demons = mutableListOf<ExtremeDemonEntity>()
        try {
            context.assets.open("demons.txt").bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val parts = line.trim().split(Regex("\\s{2,}|\t+"))
                    if (parts.size >= 10) { // 최소 데이터 개수 확인
                        val rank = parts[0].toIntOrNull() ?: 0
                        val name = parts[1]
                        val publisher = parts[2]
                        val verifier = if (parts[3] == "-") "Unknown" else parts[3]
                        // parts[4]는 어템이므로 건너뜀
                        val id = parts[5].toLongOrNull() ?: 0L
                        // parts[6]은 No Copy 여부
                        val length = parts[7]
                        val objects = parts[8]
                        // parts[9], [10]은 초당 오브젝트 및 최고 순위
                        val tier = parts[11] // Beginner 등 티어 정보
                        val song = parts.last()

                        demons.add(ExtremeDemonEntity(
                            id = id,
                            rank = rank,
                            name = name,
                            creator = publisher, // 퍼블리시어
                            verifier = verifier, // 베리파이어
                            length = length,
                            objects = objects,
                            tier = tier,
                            song = song
                        ))
                    }
                }
            }
        } catch (e: Exception) { e.printStackTrace() }
        return demons
    }
}