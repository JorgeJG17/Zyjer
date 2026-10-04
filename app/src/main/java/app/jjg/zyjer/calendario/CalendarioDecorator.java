/*
Clase CalendarioDecorator.java
Fecha actualiza: 26/03/2026
Autor: Jorge Jimenez Garrido
Descripcion: Clase para pintar los día del calendario, donde cuando llamo al constructor, es para crear un objeto con los dias y la imagen del fondo
que vamos a usar para esos dias
*/
package app.jjg.zyjer.calendario;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import androidx.core.content.ContextCompat;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

public class CalendarioDecorator implements DayViewDecorator {

    public CalendarioDecorator(Context context, int idDelDrawable, Collection<CalendarDay> dates) {
        // "Inflamos" el XML para convertirlo en un objeto Drawable real
        this.fondoCirculo = ContextCompat.getDrawable(context, idDelDrawable);
        this.dates = new HashSet<>(dates);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        //view.addSpan(new DotSpan(8, color)); // Añade un punto de 8px de radio

        // En vez de addSpan, ponemos el fondo:
        view.setBackgroundDrawable(fondoCirculo);
    }

    //private final int color;
    private final HashSet<CalendarDay> dates;
    private final Drawable fondoCirculo;
}
