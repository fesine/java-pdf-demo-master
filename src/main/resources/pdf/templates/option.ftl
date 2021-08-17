{
title: {
    text:'${title}',
    x:'middle',
    textAlign:'center'
    },
xAxis: {
    type: 'category',
    data: ${categories}
    },
yAxis: {
    type: 'value',
    axisLabel: {
        show: true,
        interval: 'auto',
        formatter: '{value}'
    }
    },
series: [{
data: ${values},
type: 'bar'
}]
}