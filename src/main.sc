require: birds.csv
    var = $Birds
    name = Birds
    
patterns:
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Help = (помоги*|помочь|помощь|не знаю)

theme: /
    state: start
        q!: * *start 
        script:
            $session.all_birds = []
            for (var i = 1; i < 4; i++) {
                $session.all_birds.push($Birds[i].value);
            }
        a: Привет. Со мной ты можешь научиться определять птиц по голосу.

        state: song
            q!: *
            script: if ($session.all_birds.length == 0) {
                        $reactions.answer("Больше мне нечего тебе загадать. Возвращайся позже."); 
                    } else if ($session.all_birds.length == 1) {
                        $session.next_bird = $session.all_birds[0];
                        $session.all_birds = [];
                    } else {
                        var i = Math.floor(Math.random()*($session.all_birds.length - 1)) + 1;
                        $session.next_bird = $session.all_birds[i];
                        $session.all_birds.splice(i, 1);
                        $reactions.answer(i); 
                    }
            if: ($session.all_birds.length != 0)
                a: {{$session.next_bird.name}}
           
            
            state: right
                q: * соловей *
                a: Верно, это {{$session.next_bird.name}}. 
                script:
                    $session.score=$session.score + 1;
                    $reactions.answer("Угадано уже '{{$session.score}}' птиц. Молодец!");
                go!: ../
    
            state: Help
                q: * $Help *
                a: Это {{$session.next_bird.name}}. 
    
            
            state: wrong
                q: *
                a: Неверно
                    
            state: Stop
                q: $Stop
                a: Было приятно сыграть.
                go!: ../../Reset

    state: Reset
        q!: (reset|* *start)
        script:
            $session = {}
            $client = {}
            $temp = {}
            $response = {}
        go!: /