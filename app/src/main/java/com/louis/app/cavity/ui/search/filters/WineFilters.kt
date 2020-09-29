package com.louis.app.cavity.ui.search.filters

import com.louis.app.cavity.model.Wine
import com.louis.app.cavity.model.relation.BottleAndWine
import com.louis.app.cavity.ui.home.WineColor
import com.louis.app.cavity.util.L
import com.louis.app.cavity.util.toBoolean

class FilterReadyToDrink : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.isReadyToDrink() }
    }
}

class ColorFilter(private val color: WineColor) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.color == Wine.getWineColorNumber(color) }
    }
}

class FilterOrganic : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.isOrganic.toBoolean() }
    }
}

class FilterCounty(private val countyId: Long) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter { it.countyId == countyId }
    }
}

class FilterDate(private val date: Long, private val searchBefore: Boolean) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return if (searchBefore)
            bottlesAndWine.filter { it.buyDate < date }
        else
            bottlesAndWine.filter { date in 0L..it.buyDate }

    }
}

class FilterText(private val query: String) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine.filter {
            val slug =
                it.name + it.naming + it.cuvee + it.buyLocation + it.otherInfo + it.tasteComment
            return@filter slug.contains(query, ignoreCase = true)
        }
    }
}

class FilterPrice(private val minPrice: Int, private val maxPrice: Int) : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        L.v(bottlesAndWine.map { it.price.toString() }.toString())
        return bottlesAndWine.filter { it.price in minPrice..maxPrice }
    }
}

class NoFilter : WineFilter {
    override fun meetFilters(bottlesAndWine: List<BottleAndWine>): List<BottleAndWine> {
        return bottlesAndWine
    }
}
