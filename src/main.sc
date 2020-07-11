patterns:
    $PlayCities = [(*ыграем|*играем)] [в] (города|город*)
    $PlayDigits = [(*ыграем|*играем)] [в] (числа|угадай число)
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Yes = (да|верно|угадал*)
    $Help = (помоги*|помочь|помощь|не знаю)

theme: /
    state: start
        q!: * *start 
        a: Привет {{$dialer.getPayload().name}}, меня зовут "Бот". Со мной вы можете весело провести время и сыграть в "Города" или "Угадай число". Скажите "Города", если хотите помериться знаниями географии или "Угадай число", чтобы загадывать и отгадывать числа.
        go: /  

    state: Cities
        q: * $PlayCities *
        script:
            $session.all_cities = []
            for (var i = 1; i < 8594; i++) {
                if ($Cities[i].value.name.match(/[А-я]/gi) != null) {
                    $session.all_cities.push($Cities[i].value.name);
                }
            }
            $session.last_letter = ""
        a: Давайте сыграем в 'Города'! Кто начинает? 
        
        state: Player
            q: я
            if: ($session.last_letter == "")
                a: Назови город.
            
            state: CityPattern
                q: $City
                script: 
                    $session.text = $parseTree.text;
                    if ($session.text.toLowerCase() != $parseTree._City.name.toLowerCase()) {
                        $reactions.answer("Это не официальное название города '{{$parseTree._City.name}}'. Не подходит.");
                    } 
                    else if (($session.last_letter != "") && ($session.text.substr(0,1).toLowerCase() != $session.last_letter.toLowerCase())) {
                        $reactions.answer("Нужно чтобы первой буквой названия была буква {{$session.last_letter}}, а у вас {{$session.text.substr(0,1)}}");
                    }
                    else if ($session.all_cities.indexOf($parseTree._City.name) == -1) {
                        $reactions.answer("Этот город уже называли. Попробуй другой");
                    } 
                    else {
                        $session.all_cities = $session.all_cities.filter(function(item) { 
                            return item !== $parseTree._City.name
                        })  
                        $session.last_letter = getLastLetter($session.text);
                        for (var i = 0; i < $session.all_cities.length; i++) {
                            if ($session.all_cities[i].substr(0,1).toLowerCase() == $session.last_letter.toLowerCase()) {
                                $session.next_city = $session.all_cities[i];
                                $session.last_letter = getLastLetter($session.next_city);
                                $session.all_cities.splice(i, 1);
                                break;
                            }
                        }
                        if ($session.next_city == "") {
                            $reactions.answer("Я сдаюсь!")
                        } else {
                            $reactions.answer("{{$session.next_city}}. Вам на {{$session.last_letter}}.")
                        }
                    }
            
            state: Help
                q: * $Help *
                script:
                    for (var i = 0; i < $session.all_cities.length; i++) {
                            if ($session.all_cities[i].substr(0,1).toLowerCase() == $session.last_letter.toLowerCase()) {
                                $session.prompt = $session.all_cities[i];
                                $reactions.answer("Попробуй {{$session.prompt}}.");
                                break;
                            }
                    }
                
            state: NoMatch
                q: *
                a: Тебе нужно назвать существующий город на букву {{$session.last_letter}}. Либо сдаться.
                
            state: Stop
                q: $Stop
                a: Было приятно сыграть.
                script:
                    $dialer.hangUp('(Бот повесил трубку)');
                
        state: Computer
            q: ты
            script: 
                var i = Math.floor(Math.random()*($session.all_cities.length - 1)) + 1;
                $session.next_city = $session.all_cities[i];
                $session.last_letter = getLastLetter($session.next_city);
                $session.all_cities.splice(i, i+1)
                
            a: Мой город {{$session.next_city}}. Вам на {{$session.last_letter}}. 
            go!: ../Player
                
        state: NoMatch
                q: *
                a: Скажите "Ты" или "Я", чтобы решить кто будет называть город первым.
        
        state: speechNotRecognized || noContext = true
                event: speechNotRecognized
                a: Скажите "Ты" или "Я", чтобы решить кто будет называть город первым.
                go!: {{$session.lastState}}
        
        state: Stop
            q: $Stop
            a: Было приятно сыграть.
            script:
                $dialer.hangUp('(Бот повесил трубку)');
            
            
    state: Digits
        q: * $PlayDigits *
        a: Давай сыграем в "Угадай число"! Кто загадывает?
    
        state: Player
            q: я
            script: 
                $session.first = Math.floor(Math.random()*100)
                $session.last = 100
            a: Загадайте число от 0 до 100. Я буду отгадывать, а вы говорите больше оно или меньше. Не забудьте сказать, когда я угадаю. Ваше число {{$session.first}}?
            
            state: Yes
                q: * $Yes *
                a: Ура!
                go!: /Digits
            
            state: More
                q: * больше *
                script:
                    $session.first = Math.floor(($session.first + $session.last)/2)
                    if ($session.first == 100) {
                        $reactions.answer("Ваше число не может превышать 100")
                    } 
                    else {
                        $reactions.answer("Ваше число больше или меньше {{$session.first}} ?")
                    }
            
            state: Less
                q: * меньше *
                script:
                    $session.last = $session.first
                    $session.first = Math.floor($session.last/2)
                    if ($session.first == 0) {
                        $reactions.answer("Ваше число не может быть меньше 0")
                    } 
                    else {
                        $reactions.answer("Ваше число больше или меньше {{$session.first}} ?")
                    }
        
            state: NoMatch
                q: *
                a: Вам нужно сказать больше или меньше число {{$session.first}} загаданного вами.
            
            state: speechNotRecognized || noContext = true
                event: speechNotRecognized
                a: Вам нужно сказать больше или меньше число {{$session.first}} загаданного вами.
                go!: {{$session.lastState}}
              
            state: Stop
                q: $Stop
                a: Было приятно сыграть.
                script:
                    $dialer.hangUp('(Бот повесил трубку)');
                
        state: Computer
            q: ты
            script:
                $session.digit = Math.floor(Math.random()*100)
                $session.previous_nums = []
            a: Загадано. Называйте число от 0 до 100, а я скажу мое больше или меньше вашего.
            
            state: NumberPattern
                q!: * $Number *
                script:
                    if (($parseTree._Number < 0) || ($parseTree._Number >100)) {
                        $reactions.answer("Мое число больше 0 и меньше 100. Попробуй еще раз.")
                    }
                    else if ($session.digit == $parseTree._Number) {
                        $reactions.answer("Ура! Вы угадали!")
                    } 
                    else if ($session.previous_nums.indexOf($parseTree._Number) == -1) {
                        $session.previous_nums.push($parseTree._Number)
                        if ($session.digit > $parseTree._Number) {
                            $reactions.answer("Мое число больше.")
                        } else {
                            $reactions.answer("Мое число меньше.")
                        }
                    } 
                    else {
                        $reactions.answer("Вы уже называли это число, попробуйте другое.")
                    }
                    
            state: NoMatch
                q: *
                a: Вам нужно назвать число от 0 до 100, чтобы отгадать.
            
            state: speechNotRecognized || noContext = true
                event: speechNotRecognized
                a: Вам нужно назвать число от 0 до 100, чтобы отгадать.
                go!: {{$session.lastState}}
            
            state: Stop
                q: $Stop
                a: Было приятно сыграть.
                script:
                    $dialer.hangUp('(Бот повесил трубку)');
                
        state: NoMatch
            q: *
            a: Скажите "Ты" или "Я", чтобы решить кто будет называть город первым.
            
        state: speechNotRecognized || noContext = true
            event: speechNotRecognized
            a: Скажите "Ты" или "Я", чтобы решить кто будет называть город первым.
            go!: {{$session.lastState}}
            
        state: Stop
            q: $Stop
            a: Было приятно сыграть.
            script:
                $dialer.hangUp('(Бот повесил трубку)');
            
    state: NoMatch
        q: *
        a: Скажите "Города", если хотите помериться знаниями географии или "Угадай число", чтобы загадывать и отгадывать числа.
    
    state: NoInput || noContext=true
        event: speechNotRecognized
        script:
            $session.noInputCounter = $session.noInputCounter || 0;
            $session.noInputCounter++;

        if: $session.noInputCounter >= 5
            a: Кажется какие-то проблемы со связью. Перезвоню вам позднее.
            script:
                $dialer.hangUp('(Бот повесил трубку)');
        else:
            a: Вас плохо слышно, повторите, пожалуйста!
    
    state: Stop
        q: $Stop
        a: Было приятно сыграть.
        script:
            $dialer.hangUp('(Бот повесил трубку)');
    
    state: ClientHungUp
        event!: hangup
        script:
            getClientHangUpReaction($session.lastState);
            
    state: reset
        q!: (reset|* *start)
        script:
            $session = {}
            $client = {}
            $temp = {}
            $response = {}
        go!: /