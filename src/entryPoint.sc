require: common.js
  module = sys.zb-common
require: patterns.sc
  module = sys.zb-common
require: text/text.sc
  module = sys.zb-common
require: number/number.sc
  module = sys.zb-common
#require: dateTime/dateTime.sc
#  module = sys.zb-common

require: params.yaml
  var = params

require: zenflow.sc
  module = sys.zfl-common

require: main.sc

require: birds.csv
    var = $Birds
    name = Birds
    
patterns:
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Help = (помоги*|помочь|помощь|не знаю)