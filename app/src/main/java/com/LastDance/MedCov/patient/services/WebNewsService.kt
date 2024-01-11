package com.LastDance.MedCov.patient.services

import com.LastDance.MedCov.patient.models.NewsData
import org.jsoup.nodes.Document
import org.jsoup.Jsoup
import org.jsoup.select.Elements

class WebNewsService {
    fun newsList() : List<NewsData> {
        val arr = mutableListOf<NewsData>()
        val url = "https://thanhnien.vn"
        val document: Document = Jsoup.connect(url).timeout(15000).get()
        val elements: Elements = document.getElementsByClass("boxStyle color-general hbBoxMainText")

        for (item in elements) {
            val img = item.getElementsByTag("img")

            val title = img.attr("alt")
            val src = img.attr("data-src")
            val href = item.attr("abs:href")


            if (title != "" && src != "" && href != "") {
                val news = NewsData(title, src, href)
                arr.add(news)
            }
            if (arr.size >= 14)
            {
                break
            }

        }
        return arr
    }
}