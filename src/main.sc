require: birds.csv
    var = $Birds
    name = Birds
    
patterns:
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Help = (помоги*|помочь|помощь|не знаю|что ты умеешь)

theme: /
    state: start
        q!: * *start 
        script:
            $session.all_birds = []
            for (var i = 1; i < 8; i++) {
                $session.all_birds.push($Birds[i].value);
            }
        audio:https://drive.google.com/uc?export=download&id=10KBFspNA3zzE4EAL8Szer8VcX8w7uezW
        a: Привет. Со мной ты можешь научиться определять птиц по голосу.

        state: song
            q!: *
            a: Попробуй угадать что это за птица.
            script: if ($session.all_birds.length == 0) {
                        $reactions.answer("Больше мне нечего тебе загадать. Возвращайся позже."); 
                    } else if ($session.all_birds.length == 1) {
                        $session.next_bird = $session.all_birds[0];
                        $session.all_birds = [];
                    } else {
                        var i = Math.floor(Math.random()*($session.all_birds.length - 1)) + 1;
                        $session.next_bird = $session.all_birds[i];
                        $session.all_birds.splice(i, 1);
                    }
            if: ($session.all_birds.length != 0)
                random:
                    audio: {{$session.next_bird.link1}}
                    audio: {{$session.next_bird.link2}}
           
            
            state: right
                q: *
                if: ($parseTree.text == $session.next_bird.name)
                    a: Верно, это {{$session.next_bird.name}}. 
                    script:
                        $session.score=$session.score + 1;
                        $reactions.answer("Угадано уже '{{$session.score}}' птиц. Молодец!");
                    go!: ../
                else:
                    go!: ../wrong
    
            state: Help
                q: * $Help *
                a: Это {{$session.next_bird.name}}. 
    
            
            state: wrong
                q: *
                a: Неверно. Еще разок
                random:
                    audio: {{$session.next_bird.link1}}
                    audio: {{$session.next_bird.link2}}
                    
            state: Stop
                q: $Stop
                a: Было приятно сыграть.
                go: /Reset

            state: Reset
                q!: (reset|* *start)
                a: сброс
                script:
                    $session = {}
                    $client = {}
                    $temp = {}
                    $response = {}
                go!: ../../start
    state: help
            q: * $Help *
            a: Это навык орнитология. Я могу помочь тебе научиться определять птиц по голосам.