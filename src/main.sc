require: birds.csv
    var = $Birds
    name = Birds
    
patterns:
    $Stop = (хватит|перестань|прекрати|закончим|сдаюсь|стоп|пока)
    $Help = (помоги*|помочь|помощь|не знаю|что ты умеешь)
    $BirdList = (ласточка|стриж|курица|сойка|кукушка)

theme: /
    state: start
        q!: * *start 
        script:
            $session.all_birds = []
            for (var i = 1; i < 2; i++) {
                $session.all_birds.push($Birds[i].value);
            }
            $session.score=0;

        a: Привет. Со мной вы можете научиться определять птиц по голосу. Я включаю запись, а вы угадываете что это за птица. Начнем?

        state: song
            q: Да 
            script: if ($session.all_birds.length == 0) {
                        $reactions.answer("Больше мне нечего вам загадать. Возвращайтесь позже."); 
                    } else if ($session.all_birds.length == 1) {
                        $session.next_bird = $session.all_birds[0];
                        $session.all_birds = [];
                    } else {
                        var i = Math.floor(Math.random()*($session.all_birds.length - 1)) + 1;
                        $session.next_bird = $session.all_birds[i];
                        $session.all_birds.splice(i, 1);
                    }
            if: ($session.all_birds.length != 0)
                a: Попробуйте угадать что это за птица.

                random:
                    audio: {{$session.next_bird.link1}}
                    audio: {{$session.next_bird.link2}}
           
            
            state: right
                q: *
                if: ($parseTree.text == $session.next_bird.name)
                    a: Верно, это {{$session.next_bird.name}}. 
                    script:
                        $session.score=$session.score + 1;
                        $reactions.answer("Получилось узнать уже '{{$session.score}}'. Молодец!");
                    go!: ../
                else:
                    go!: ../wrong
    
            state: Help
                q: * $Help *
                a: Это {{$session.next_bird.name}}. 
    
            
            state: wrong
                a: Неверно. Включить еще разок?
                state: Yes
                    q: Да
                    a: Включаю еще раз.
                    random:
                        audio: {{$session.next_bird.link1}}
                        audio: {{$session.next_bird.link2}}
                    go: ../
                        
                
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
                
        state: No
            q: *
            a: Скажите "да", когда будете готовы.
                
        state: what
                q: Что ты умеешь
                a: Я могу помочь вам научиться определять птиц по голосам. Я включаю запись, а вы угадываете что это за птица. Начнем?
        state: help
                q: Помощь
                a: Это навык "Занимательная орнитология". Я могу помочь вам научиться определять птиц по голосам. Я включаю запись, а вы угадываете что это за птица. Начнем? 