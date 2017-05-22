package com.example.alex.internationalproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TemperatureGraph extends AppCompatActivity {
    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature_graph);

        //int[] temps = new int[] {37,37,37,38,38,38,38,37,36,36,37};

        List<Integer> temps = Arrays.asList(37,37,37,38,38,38,38,37,36,36,37);

        int min = Collections.min(temps);
        int max = Collections.max(temps);

        DataPoint[] points = new DataPoint[temps.size()];
        for(int i = 0; i < temps.size(); i++) {
            points[i] = new DataPoint(i, temps.get(i));
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);

        graph = (GraphView) findViewById(R.id.graph);

        graph.setTitle("Temperatures between XX and XX");

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(min - 1);
        graph.getViewport().setMaxY(max + 1);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(points.length);

        graph.getViewport().setScalable(true);
        graph.getViewport().setScalableY(true);

        graph.addSeries(series);
    }
}
